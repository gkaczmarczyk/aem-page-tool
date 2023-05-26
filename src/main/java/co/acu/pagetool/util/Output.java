package co.acu.pagetool.util;

/**
 * Simple class to "prettify" the output for readability
 */
public class Output {

    public static final String OK = Colors.FG_GREEN + "OK" + Colors.FG_RESET;
    public static final String NOT_OK = Colors.FG_RED + "NOT OK" + Colors.FG_RESET;

    public static Output line() {
        System.out.println();
        return null;
    }

    public static Output ninfo(String message) {
        System.out.print(message);
        return null;
    }

    /**
     * Outputs the given string to the console in yellow text
     * @param message the String to write
     */
    public static Output info(String message) {
        Output.ninfo(message);
        Output.line();
        return null;
    }

    public static Output nhl(String message) {
        System.out.print(Colors.FG_YELLOW + message + Colors.FG_RESET);
        return null;
    }

    /**
     * Outputs the given string to the console in yellow text
     * @param message the String to write
     */
    public static Output hl(String message) {
        Output.ninfo(message);
        Output.line();
        return null;
    }

    public static Output nwarn(String message) {
        System.out.print(Colors.FG_RED + message + Colors.FG_RESET);
        return null;
    }

    /**
     * Outputs the given string to the console in red text
     * @param message the String to write
     */
    public static Output warn(String message) {
        Output.nwarn(message);
        Output.line();
        return null;
    }

    static class Colors {

        public static final String FG_RESET = "\u001B[0m";
        public static final String FG_RED = "\u001B[31m";
        public static final String FG_GREEN = "\u001B[32m";
        public static final String FG_YELLOW = "\u001B[33m";

    }

}
