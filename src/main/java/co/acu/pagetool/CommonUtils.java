package co.acu.pagetool;

/**
 * This class contains a set of small, useful utility methods
 * @author Greg Kaczmarczyk
 */
public class CommonUtils {

    /**
     * Execute a shell command
     * @param cmd An array of the command & its arguments
     * @return The output of the command
     * @throws java.io.IOException
     */
    public static String execCommand(String[] cmd) throws java.io.IOException {
        Process proc = Runtime.getRuntime().exec(cmd);
        java.io.InputStream is = proc.getInputStream();
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return (s.hasNext()) ? s.next().trim() : "";
    }

}
