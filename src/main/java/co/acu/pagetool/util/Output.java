package co.acu.pagetool.util;

/**
 * Simple class to "prettify" the output for readability
 */
public class Output {

    public static final String OK = Colors.FG_GREEN + "OK" + Colors.FG_RESET;
    public static final String NOT_OK = Colors.FG_RED + "NOT OK" + Colors.FG_RESET;

    public static void line() {
        System.out.println();
    }

    /**
     * Outputs the given string to the console in regular text with no newline
     * @param message the String to write
     */
    public static void ninfo(String message) {
        System.out.print(message);
    }

    /**
     * Outputs the given string to the console in regular text
     * @param message the String to write
     */
    public static void info(String message) {
        Output.ninfo(message);
        Output.line();
    }

    /**
     * Outputs the given string to the console in green text with no newline
     * @param message the String to write
     */
    public static void nnote(String message) {
        System.out.print(Colors.FG_GREEN + message + Colors.FG_RESET);
    }

    /**
     * Outputs the given string to the console in green text
     * @param message the String to write
     */
    public static void note(String message) {
        Output.nnote(message);
        Output.line();
    }

    /**
     * Outputs the given string to the console in yellow text with no newline
     * @param message the String to write
     */
    public static void nhl(String message) {
        System.out.print(Colors.FG_YELLOW + message + Colors.FG_RESET);
    }

    /**
     * Outputs the given string to the console in yellow text
     * @param message the String to write
     */
    public static void hl(String message) {
        Output.nhl(message);
        Output.line();
    }

    /**
     * Outputs the given string to the console in red text with no newline
     * @param message the String to write
     */
    public static void nwarn(String message) {
        System.out.print(Colors.FG_RED + message + Colors.FG_RESET);
    }

    /**
     * Outputs the given string to the console in red text
     * @param message the String to write
     */
    public static void warn(String message) {
        Output.nwarn(message);
        Output.line();
    }

    static class Colors {

        public static final String FG_RESET = "\u001B[0m";
        public static final String FG_RED = "\u001B[31m";
        public static final String FG_GREEN = "\u001B[32m";
        public static final String FG_YELLOW = "\u001B[33m";

    }

}
