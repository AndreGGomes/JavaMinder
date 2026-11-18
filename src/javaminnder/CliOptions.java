package javaminnder;

import java.nio.file.Path;
import javaminnder.exception.ValidationException;

/** Opções de linha de comando do JavaMinder. */
public final class CliOptions {

    private final Path dataDirectory;
    private final boolean seedDemo;
    private final boolean help;

    private CliOptions(Path dataDirectory, boolean seedDemo, boolean help) {
        this.dataDirectory = dataDirectory;
        this.seedDemo = seedDemo;
        this.help = help;
    }

    public static CliOptions parse(String[] args) {
        Path dataDirectory = Path.of("data");
        boolean seedDemo = false;
        boolean help = false;

        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            switch (argument) {
                case "--data", "-d" -> {
                    if (index + 1 >= args.length) {
                        throw new ValidationException("Informe o diretório depois de " + argument + ".");
                    }
                    dataDirectory = Path.of(args[++index]);
                }
                case "--seed-demo" -> seedDemo = true;
                case "--help", "-h" -> help = true;
                default -> throw new ValidationException("Opção desconhecida: " + argument);
            }
        }
        return new CliOptions(dataDirectory, seedDemo, help);
    }

    public Path dataDirectory() {
        return dataDirectory;
    }

    public boolean seedDemo() {
        return seedDemo;
    }

    public boolean help() {
        return help;
    }
}
