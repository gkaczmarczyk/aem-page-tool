package co.acu.pagetool;

import co.acu.pagetool.crx.CrxConnection;
import co.acu.pagetool.crx.CrxConnectionFactory;
import org.apache.commons.cli.*;

import java.io.PrintWriter;

/**
 * Node Tool allows updating JCR properties en masse easily.
 */
public class PageToolApp {

    static boolean verbose = false;
    static boolean dryRun = false;

    public static void main(String[] args) {
        Options options = new Options();
        options
                .addOption("u", true, "Credentials: The username for logging into AEM (default is admin)")
                .addOption("w", true, "Credentials: The password for logging into AEM (default is admin)")
                .addOption("l", true, "Credentials: A combination of username & password for logging into AEM (format is admin:admin)")
                .addOption("h", true, "Server: The hostname of the AEM instance to be accessed (default is localhost)")
                .addOption("t", true, "Server: The port of the AEM instance to be accessed (default is 4502)")
                .addOption("s", true, "Server: A combination of hostname & port of the AEM instance to be accessed (format is localhost:4502)")
                .addOption("c", true, "Credentials: A shorthand combination of username, password, hostname, & port (format is admin:admin@localhost:4502)")
                .addOption("n", true, "The parent node of the nodes expected to be updated. Nodes updated will only be descendents of this provided node.")
                .addOption("m", true, "The node to be updated must contain the specified property & its corresponding value (format property=value). Any number of matching properties can be used.")
                .addOption("p", true, "The property name & value to be updated on the nodes (format is property=value). Any number of properties can be used.")
                .addOption("i", true, "Copy Values: The property name from which a value should be copied (Must be used with -o option)")
                .addOption("o", true, "Copy Values: The property name to which the value should be copied (Must be used with -i option)")
                .addOption("d", true, "The property name & value to be deleted on the nodes (format is property=value). Any number of properties can be used.")
                .addOption("y", false, "Perform a dry-run of the command. This will perform all get functions, but will not execute update or delete operations.")
                .addOption("x", false, "Output more verbosely");

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (!cmd.hasOption('n') && !cmd.hasOption('p')) {
                PageToolApp.printHelp(options);
                return;
            }
            if (!cmd.hasOption('n')) {
                System.out.println("Parent node (-n) is a required argument.");
                return;
            }
            if ((cmd.hasOption('i') && !cmd.hasOption("o")) || (!cmd.hasOption("i") && cmd.hasOption("o"))) {
                System.out.println("A 'copy from' property (-i) must be specified together with a 'copy to' property (-o).");
                return;
            }
            if (!cmd.hasOption('p') && !cmd.hasOption('d') && !cmd.hasOption("i")) {
                System.out.println("Property to update or delete (-p or -d) is a required argument.");
                return;
            }
            if (cmd.hasOption('x')) {
                PageToolApp.verbose = true;
            }
            if (cmd.hasOption('y')) {
                PageToolApp.dryRun = true;
            }

            CrxConnection conn = CrxConnectionFactory.getCrxConnection(cmd);

            PageTool nodeTool = new PageTool(cmd.getOptionValue('n'));
            nodeTool.setConnection(conn);

            if (cmd.hasOption('m')) {
                nodeTool.setMatchingProperties(cmd.getOptionValues('m'));
            }
            if (cmd.hasOption('i')) {
                nodeTool.setCopyFromProperties(cmd.getOptionValues('i'));
            }
            if (cmd.hasOption('o')) {
                nodeTool.setCopyToProperties(cmd.getOptionValues('o'));
            }
            if (cmd.hasOption('p')) {
                nodeTool.setUpdateProperties(cmd.getOptionValues('p'));
            }
            if (cmd.hasOption('d')) {
                nodeTool.setDeleteProperties(cmd.getOptionValues('d'));
            }
            nodeTool.run();
        } catch (Exception e) {
            System.out.println("Error parsing options (" + e.toString() + ")");
            if (PageToolApp.verbose) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Prints help message to the standard out
     * @param options Command line options
     */
    public static void printHelp(Options options) {
        int cols = 80;
        try {
            String colVal = CommonUtils.execCommand(new String[] { "bash", "-c", "tput cols 2> /dev/tty" });
            int calcCols = Integer.parseInt(colVal);
            if (calcCols > cols) {
                cols = calcCols;
            }
        } catch (Exception e) {
            // do nothing
        }

        PrintWriter pw = new PrintWriter(System.out);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(pw, cols, "pagetool -n /path/to/parent/page -p property=value [-p p=v ...] [OPTIONS]", "Available options:", options, 2, 4, null);
        pw.flush();
        pw.close();
    }

}
