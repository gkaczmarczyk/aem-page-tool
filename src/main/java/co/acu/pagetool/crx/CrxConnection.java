package co.acu.pagetool.crx;

/**
 * Connection class to manage the connection information for the AEM host
 * @author Gregory Kaczmarczyk
 */
public class CrxConnection {

    static final String DEFAULT_USER = "admin";
    static final String DEFAULT_PASSWORD = "admin";
    static final String DEFAULT_HOST = "localhost";
    static final String DEFAULT_PORT = "4502";

    private String username;
    private String password;
    private String hostname;
    private String port;

    public CrxConnection() {
        this.username = DEFAULT_USER;
        this.password = DEFAULT_PASSWORD;
        this.hostname = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
    }

    public CrxConnection(String username, String password) {
        this.username = (username == null || username.equals("")) ? DEFAULT_USER : username;
        this.password = (password == null || password.equals("")) ? DEFAULT_PASSWORD : password;
        this.hostname = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
    }

    public CrxConnection(String username, String password, String hostname, String port) {
        this.username = (username == null || username.equals("")) ? DEFAULT_USER : username;
        this.password = (password == null || password.equals("")) ? DEFAULT_PASSWORD : password;
        this.hostname = (hostname == null || hostname.equals("")) ? DEFAULT_HOST : hostname;
        this.port = (port == null || port.equals("")) ? DEFAULT_PORT : port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username != null && !username.equals("")) {
            this.username = username;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null && !password.equals("")) {
            this.password = password;
        }
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        if (hostname != null && !hostname.equals("")) {
            this.hostname = hostname;
        }
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        if (port != null && !port.equals("")) {
            this.port = port;
        }
    }

}
