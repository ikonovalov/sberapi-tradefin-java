package ru.codeunited.sbapi.escrow.tools;

public class Console {

    public static final String ANSI_RESET = "\u001B[0m";

    // Colors
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    // Background
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    // Bold
    public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    public static void error(String msg) {
        System.err.println(msg);
    }

    public static void info(String msg) {
        System.out.println(msg);
    }

    public static void ok(String msg) {
        info(green("[OK]") + "\t" + whiteBold(msg));
    }

    public static void fail(String msg) {
        System.err.println(red("[FAIL]\t" + msg));
        System.exit(1);
    }

    private static String red(String str) {
        return ANSI_RED + str + ANSI_RESET;
    }

    private static String green(String str) {
        return ANSI_GREEN + str + ANSI_RESET;
    }

    public static String whiteBold(String msg) {
        return WHITE_BOLD + msg + ANSI_RESET;
    }
}
