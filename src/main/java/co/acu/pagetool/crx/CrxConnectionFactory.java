package co.acu.pagetool.crx;

import org.apache.commons.cli.CommandLine;

/**
 * Factory for CRX Connection class
 * @author Gregory Kaczmarczyk
 */
public class CrxConnectionFactory {

    public static CrxConnection getCrxConnection(CommandLine cmd) {
        CrxConnection conn = new CrxConnection();

        if (cmd.hasOption('c')) {
            String[] connection = cmd.getOptionValue('c').split("@");
            if (connection.length >= 2) {
                String[] login = connection[0].split(":");
                if (login.length >= 2) {
                    conn.setUsername(login[0]);
                    conn.setPassword(login[1]);
                }
                String[] server = connection[1].split(":");
                if (server.length >= 2) {
                    conn.setHostname(server[0]);
                    conn.setPort(server[1]);
                }
            }
        } else {
            if (cmd.hasOption('l')) {
                String[] login = cmd.getOptionValue('l').split(":");
                if (login.length >= 2) {
                    conn.setUsername(login[0]);
                    conn.setPassword(login[1]);
                }
            } else {
                if (cmd.hasOption('u')) {
                    conn.setUsername(cmd.getOptionValue('u'));
                }
                if (cmd.hasOption('w')) {
                    conn.setPassword(cmd.getOptionValue('w'));
                }
            }
            if (cmd.hasOption('s')) {
                String[] server = cmd.getOptionValue('s').split(":");
                if (server.length >= 2) {
                    conn.setHostname(server[0]);
                    conn.setPort(server[1]);
                }
            } else {
                if (cmd.hasOption('h')) {
                    conn.setHostname(cmd.getOptionValue('h'));
                }
                if (cmd.hasOption('t')) {
                    conn.setPort(cmd.getOptionValue('t'));
                }
            }
        }

        return conn;
    }

}
