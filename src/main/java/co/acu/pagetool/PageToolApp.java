package co.acu.pagetool;

import co.acu.pagetool.crx.CrxConnection;
import co.acu.pagetool.crx.CrxConnectionFactory;
import org.apache.commons.cli.*;

/**
 * Node Tool allows updating JCR properties en masse easily.
 */
public class PageToolApp {

    public static void main(String[] args) {
        Options options = new Options();
        options
                .addOption("u", true, "The username for logging into CQ (default is admin)")
                .addOption("w", true, "The password for logging into CQ (default is admin)")
                .addOption("l", true, "A combination of username & password for logging into CQ (format is admin:admin)")
                .addOption("h", true, "The hostname of the CQ instance to be accessed (default is localhost)")
                .addOption("t", true, "The port of the CQ instance to be accessed (default is 4502)")
                .addOption("s", true, "A combination of hostname & port of the CQ instance to be accessed (format is localhost:4502)")
                .addOption("c", true, "A shorthand combination of username, password, hostname, & port (format is admin:admin@localhost:4502)")
                .addOption("n", true, "The parent node of the nodes expected to be updated")
                .addOption("m", true, "The node to be updated must contain the specified property & its corresponding value")
                .addOption("p", true, "The property name & value to be updated on the nodes (separated by an '=')");

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (!cmd.hasOption('p')) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("pagetool", options);
                return;
            }
            if (!cmd.hasOption('n')) {
                System.out.println("Parent node is a required argument.");
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

}
