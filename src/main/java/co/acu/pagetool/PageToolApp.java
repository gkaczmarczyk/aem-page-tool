package co.acu.pagetool;

import co.acu.pagetool.crx.CrxConnection;
import co.acu.pagetool.crx.CrxConnectionFactory;
import org.apache.commons.cli.*;

import java.io.PrintWriter;

/**
 * Node Tool allows updating JCR properties en masse easily.
 */
public class PageToolApp {

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
                .addOption("p", true, "The property name & value to be updated on the nodes (format is property=value). Any number of properties can be used.");

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
            if (!cmd.hasOption('p')) {
                System.out.println("Property (-p) is a required argument.");
                return;
            }

            CrxConnection conn = CrxConnectionFactory.getCrxConnection(cmd);

            PageTool nodeTool = new PageTool(cmd.getOptionValue('n'));
            nodeTool.setConnection(conn);

            if (cmd.hasOption('m')) {
                nodeTool.setMatchingProperties(cmd.getOptionValues('m'));
            }
            nodeTool.setUpdateProperties(cmd.getOptionValues('p'));
            nodeTool.run();
        } catch (Exception e) {
            System.out.println("Error parsing options (" + e.toString() + ")");
            e.printStackTrace();
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