package javaminnder.ui;

/** Formatação da saída dos menus de terminal. */
public final class Menu {

    private static final int WIDTH = 64;

    private Menu() {
    }

    public static String header(String title) {
        return "\n" + "=".repeat(WIDTH) + "\n  " + title + "\n" + "=".repeat(WIDTH);
    }

    public static String option(int number, String label) {
        return "  " + number + " - " + label;
    }

    public static String message(String text) {
        return "  " + text;
    }

    public static String error(String text) {
        return "  ! " + text;
    }
}
