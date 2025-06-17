package co.acu.pagetool;

import co.acu.pagetool.crx.*;
import co.acu.pagetool.exception.InvalidPropertyException;
import co.acu.pagetool.util.Output;
import co.acu.pagetool.util.Util;
import org.apache.commons.cli.*;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Entry point for the PageTool CLI application, which allows mass updates to JCR properties in AEM.
 */
public class PageToolApp {

    public static boolean bypassSSL = false;
    public static boolean verbose = false;
    public static boolean dryRun = false;

    public static void main(String[] args) {
        Options options = buildOptions();
        CommandLine cmd = parseCommandLine(options, args);
        if (cmd == null) {
            return;
        }

        boolean secure = cmd.hasOption('S');
        bypassSSL = cmd.hasOption('C');
        verbose = cmd.hasOption('x');
        dryRun = cmd.hasOption('y');

        CrxConnection conn = CrxConnectionFactory.getCrxConnection(cmd, secure);
        if (conn == null) {
            Output.warn("Failed to configure AEM connection.");
            return;
        }

        OperationProperties props = new OperationProperties();
        String parentNodePath = cmd.getOptionValue('n');
        if (!configureProperties(props, cmd, options)) {
            return; // Exit if property configuration fails
        }

        SlingClient slingClient = new SlingClient(conn, props, new QueryUrl(conn));
        PageTool pageTool = new PageTool(parentNodePath, conn, slingClient);

        pageTool.setProperties(props);
        pageTool.setIsPropertyPath(cmd.hasOption('P'));
        pageTool.executeOperation();
    }

    private static Options buildOptions() {
        Options options = new Options();
        options.addOption("u", true, "Credentials: Username for AEM (default: admin)")
                .addOption("w", true, "Credentials: Password for AEM (default: admin)")
                .addOption("l", true, "Credentials: Username:password combo (e.g. admin:admin)")
                .addOption("h", true, "Server: AEM hostname (default: localhost)")
                .addOption("t", true, "Server: AEM port (default: 4502)")
                .addOption("s", true, "Server: Hostname:port combo (e.g. localhost:4502)")
                .addOption("c", true, "Credentials: Full combo (e.g. admin:admin@localhost:4502)")
                .addOption("S", false, "Use HTTPS instead of HTTP")
                .addOption("C", false, "Bypass SSL certificate checking")
                .addOption("n", true, "Parent node path for updates (required)")
                .addOption("m", "match", true, "Match nodes with property=value (multiple allowed)")
                .addOption("p", true, "Property to update (property=value, multiple allowed)")
                .addOption("i", "copy-from", true, "Property to copy from (use with -o)")
                .addOption("o", "copy-to", true, "Property to copy to (use with -i)")
                .addOption("a", "add-node", true, "Create node with name=jcr:primaryType (e.g. newNode=nt:unstructured)")
                .addOption("P", "page", false, "Restrict to cq:Page nodes (default: all node types)")
                .addOption("r", "replace", true, "Replace string in -p property with this value")
                .addOption("d", "delete", true, "Property to delete")
                .addOption("f", "find", true, "Search criteria (node_name or property=value)")
                .addOption("y", false, "Perform a dry run (no updates)")
                .addOption("x", false, "Verbose output");
        return options;
    }

    private static CommandLine parseCommandLine(Options options, String[] args) {
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (!validateOptions(cmd)) {
                printHelp(options);
                return null;
            }
            return cmd;
        } catch (ParseException e) {
            Output.warn("Error parsing options: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            printHelp(options);
            return null;
        }
    }

    private static boolean validateOptions(CommandLine cmd) {
        if (!cmd.hasOption('n')) {
            Output.warn("Parent node (-n) is required.");
            return false;
        }
        if (!cmd.hasOption('p') && !cmd.hasOption('d') && !cmd.hasOption("i") && !cmd.hasOption('f') && !cmd.hasOption('a')) {
            Output.warn("At least one operation (-p, -d, -i, -f, or -a) is required.");
            return false;
        }
        if ((cmd.hasOption('i') && !cmd.hasOption("o")) || (!cmd.hasOption("i") && cmd.hasOption("o"))) {
            Output.warn("Both 'copy from' (-i) and 'copy to' (-o) must be specified together.");
            return false;
        }
        if (cmd.hasOption('a') && !cmd.hasOption('m')) {
            Output.warn("Node creation (-a) requires matching properties (-m).");
            return false;
        }
        return true;
    }

    /**
     * Configures OperationProperties from command-line options, handling exceptions gracefully.
     *
     * @param props   The properties object to configure
     * @param cmd     Parsed command line
     * @param options Available CLI options for descriptions
     * @return True if configuration succeeds, false if a fatal error occurs
     */
    private static boolean configureProperties(OperationProperties props, CommandLine cmd, Options options) {
        if (cmd.hasOption('P')) {
            props.setCqPageType(true);
        }

        try {
            if (cmd.hasOption('m')) {
                setProperty(props::setMatchingProperties, cmd.getOptionValues('m'), options.getOption("m"), "matching");
            }
            if (cmd.hasOption('f')) {
                props.setSearchValue(cmd.getOptionValues('f'));
            }
            if (cmd.hasOption('i')) {
                props.setCopyFromProperties(cmd.getOptionValues('i'));
            }
            if (cmd.hasOption('o')) {
                props.setCopyToProperties(cmd.getOptionValues('o'));
            }
            if (cmd.hasOption('p') && !cmd.hasOption('r')) {
                String[] updateProps = cmd.getOptionValues('p');
                setProperty(props::setUpdateProperties, updateProps, options.getOption("p"), "update");
                if (cmd.hasOption('f')) {
                    ArrayList<Property> updateList = props.getUpdateProperties();
                    for (Property prop : updateList) {
                        if (prop.getValue() != null && prop.getValue().matches("^\\[.*\\]$")) {
                            String val = prop.getValue().replaceAll("^\\[|\\]$", "");
                            prop.setValues(new String[]{val});
                            prop.setMulti(true);
                        }
                    }
                }
            }
            if (cmd.hasOption('r')) {
                props.setPropertyValueReplacement(cmd.getOptionValues('p'), cmd.getOptionValues('r'));
            }
            if (cmd.hasOption('d')) {
                setProperty(props::setDeleteProperties, cmd.getOptionValues('d'), options.getOption("d"), "delete");
            }
            if (cmd.hasOption('a')) {
                setProperty(props::setCreateNode, cmd.getOptionValues('a'), options.getOption("a"), "create node");
            }
        } catch (Exception e) {
            Output.warn("Failed to configure properties: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    /**
     * Helper method to set a property with exception handling and descriptive error messages.
     *
     * @param setter     The setter method to call (e.g. props::setMatchingProperties)
     * @param values     The values to set
     * @param option     The CLI option for context
     * @param propertyType The type of property (e.g. "matching", "update")
     * @throws InvalidPropertyException If the property format is invalid
     * @throws Exception                For other unexpected errors
     */
    private static void setProperty(PropertySetter setter, String[] values, Option option, String propertyType) throws Exception {
        try {
            setter.set(values);
        } catch (InvalidPropertyException e) {
            String message = String.format("Invalid %s property '%s': %s (%s)",
                    propertyType, e.getMessage(), option.getDescription(), "provided: " + String.join(", ", values));
            throw new InvalidPropertyException(message, e);
        }
    }

    private static void printHelp(Options options) {
        int cols = 80;
        try {
            String colVal = Util.execCommand(new String[]{"bash", "-c", "tput cols 2> /dev/tty"});
            int calcCols = Integer.parseInt(colVal.trim());
            if (calcCols > cols) {
                cols = calcCols;
            }
        } catch (Exception ignored) {
            // Default to 80 if tput fails
        }
        PrintWriter pw = new PrintWriter(System.out);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(pw, cols, "pagetool -n /path/to/parent/page -p property=value [-p p=v ...] [OPTIONS]",
                "Available options:", options, 2, 4, null);
        pw.flush();
        pw.close();
    }

    @FunctionalInterface
    private interface PropertySetter {
        void set(String[] values) throws Exception;
    }

}
