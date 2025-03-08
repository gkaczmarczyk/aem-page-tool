package co.acu.pagetool.crx;

/**
 * Connection class to manage the connection information for the AEM host.
 *
 * @author Gregory Kaczmarczyk
 */
public class CrxConnection {

    static final String DEFAULT_USER = "admin";
    static final String DEFAULT_PASSWORD = "admin";
    static final String DEFAULT_HOST = "localhost";
    static final String DEFAULT_PORT = "4502";

    private final String username;
    private final String password;
    private final String hostname;
    private final String port;
    private final boolean secure;

    public CrxConnection() {
        this.username = DEFAULT_USER;
        this.password = DEFAULT_PASSWORD;
        this.hostname = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
        this.secure = false; // Default to HTTP
    }

    public CrxConnection(String username, String password) {
        this.username = (username == null || username.isEmpty()) ? DEFAULT_USER : username;
        this.password = (password == null || password.isEmpty()) ? DEFAULT_PASSWORD : password;
        this.hostname = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
        this.secure = false;
    }

    public CrxConnection(String username, String password, String hostname, String port, boolean secure) {
        this.username = (username == null || username.isEmpty()) ? DEFAULT_USER : username;
        this.password = (password == null || password.isEmpty()) ? DEFAULT_PASSWORD : password;
        this.hostname = (hostname == null || hostname.isEmpty()) ? DEFAULT_HOST : hostname;
        this.port = (port == null || port.isEmpty()) ? DEFAULT_PORT : port;
        this.secure = secure;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }

    public boolean isSecure() {
        return secure;
    }

}
