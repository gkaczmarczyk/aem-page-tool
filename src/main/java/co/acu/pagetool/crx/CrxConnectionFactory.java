package co.acu.pagetool.crx;

import org.apache.commons.cli.CommandLine;

/**
 * Factory for creating immutable CrxConnection instances based on command-line options.
 *
 * @author Gregory Kaczmarczyk
 */
public class CrxConnectionFactory {

    /**
     * Creates a CrxConnection instance from command-line options.
     *
     * @param cmd    Parsed command-line options
     * @param secure Whether to use HTTPS (from -S option)
     * @return A configured CrxConnection instance
     */
    public static CrxConnection getCrxConnection(CommandLine cmd, boolean secure) {
        String username = CrxConnection.DEFAULT_USER;
        String password = CrxConnection.DEFAULT_PASSWORD;
        String hostname = CrxConnection.DEFAULT_HOST;
        String port = CrxConnection.DEFAULT_PORT;

        if (cmd.hasOption('c')) {
            String[] connection = cmd.getOptionValue('c').split("@");
            if (connection.length < 2) {
                throw new IllegalArgumentException("Invalid -c format; expected username:password@hostname:port");
            }
            String[] login = connection[0].split(":");
            if (login.length < 2) {
                throw new IllegalArgumentException("Invalid -c login format; expected username:password");
            }
            username = login[0];
            password = login[1];
            String[] server = connection[1].split(":");
            if (server.length < 2) {
                throw new IllegalArgumentException("Invalid -c server format; expected hostname:port");
            }
            hostname = server[0];
            port = server[1];
        } else {
            if (cmd.hasOption('l')) {
                String[] login = cmd.getOptionValue('l').split(":");
                if (login.length < 2) {
                    throw new IllegalArgumentException("Invalid -l format; expected username:password");
                }
                username = login[0];
                password = login[1];
            } else {
                if (cmd.hasOption('u')) {
                    username = cmd.getOptionValue('u');
                }
                if (cmd.hasOption('w')) {
                    password = cmd.getOptionValue('w');
                }
            }
            if (cmd.hasOption('s')) {
                String[] server = cmd.getOptionValue('s').split(":");
                if (server.length < 2) {
                    throw new IllegalArgumentException("Invalid -s format; expected hostname:port");
                }
                hostname = server[0];
                port = server[1];
            } else {
                if (cmd.hasOption('h')) {
                    hostname = cmd.getOptionValue('h');
                }
                if (cmd.hasOption('t')) {
                    port = cmd.getOptionValue('t');
                }
            }
        }

        return new CrxConnection(username, password, hostname, port, secure);
    }

}
