package javaminnder.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;

/** Centraliza toda a conversa com o terminal, sem depender de nada além da JDK. */
public class Console {

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Scanner scanner;
    private final PrintStream out;

    public Console() {
        this(System.in, System.out);
    }

    public Console(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in, "UTF-8");
        this.out = out;
    }

    public void println(String text) {
        out.println(text);
    }

    public void println() {
        out.println();
    }

    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        if (!scanner.hasNextLine()) {
            throw new AppExitException("Entrada encerrada.");
        }
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            long value = readLong(prompt);
            if (value < min || value > max) {
                println("  Escolha um número entre " + min + " e " + max + ".");
                continue;
            }
            return (int) value;
        }
    }

    public long readLong(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException exception) {
                println("  Valor inválido. Digite um número.");
            }
        }
    }

    public BigDecimal readMoney(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            String normalized = raw.contains(",") ? raw.replace(".", "").replace(',', '.') : raw;
            try {
                BigDecimal value = new BigDecimal(normalized.trim());
                if (value.signum() <= 0) {
                    println("  O valor precisa ser maior que zero.");
                    continue;
                }
                return value.setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException exception) {
                println("  Valor inválido. Exemplo: 44,90");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            if (raw.isBlank()) {
                println("  Informe uma data.");
                continue;
            }
            try {
                if (raw.contains("/")) {
                    return LocalDate.parse(raw, BR_DATE);
                }
                return LocalDate.parse(raw);
            } catch (DateTimeParseException exception) {
                println("  Data inválida. Use o formato dd/MM/aaaa.");
            }
        }
    }

    public boolean confirm(String prompt) {
        while (true) {
            String raw = readLine(prompt + " (s/n): ").toLowerCase(Locale.ROOT);
            if (raw.equals("s") || raw.equals("sim")) {
                return true;
            }
            if (raw.equals("n") || raw.equals("nao") || raw.equals("não")) {
                return false;
            }
            println("  Responda com 's' ou 'n'.");
        }
    }
}
