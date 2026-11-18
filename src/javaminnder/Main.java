package javaminnder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javaminnder.repository.SubscriptionRepository;
import javaminnder.repository.UserRepository;
import javaminnder.security.Pbkdf2PasswordHasher;
import javaminnder.service.AuthService;
import javaminnder.service.DashboardService;
import javaminnder.service.ExportService;
import javaminnder.service.Session;
import javaminnder.service.SubscriptionService;
import javaminnder.ui.App;
import javaminnder.ui.AppExitException;
import javaminnder.ui.Console;

/**
 * Ponto de entrada do JavaMinder.
 *
 * <p>É também o único lugar que conhece as implementações concretas: monta
 * repositórios, serviços e telas e entrega tudo pronto para a {@link App}.</p>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Console console = new Console();

        CliOptions options;
        try {
            options = CliOptions.parse(args);
        } catch (RuntimeException exception) {
            System.err.println("Erro: " + exception.getMessage());
            printHelp();
            return;
        }

        if (options.help()) {
            printHelp();
            return;
        }

        try {
            Path dataDirectory = Files.createDirectories(options.dataDirectory());

            UserRepository userRepository = new UserRepository(dataDirectory.resolve("users.json"));
            SubscriptionRepository subscriptionRepository =
                    new SubscriptionRepository(dataDirectory.resolve("subscriptions.json"));

            Pbkdf2PasswordHasher passwordHasher = new Pbkdf2PasswordHasher();
            Session session = new Session();

            AuthService authService = new AuthService(userRepository, passwordHasher, session);
            SubscriptionService subscriptionService =
                    new SubscriptionService(subscriptionRepository);
            DashboardService dashboardService = new DashboardService(subscriptionService);
            ExportService exportService =
                    new ExportService(subscriptionService, dataDirectory.resolve("exports"));

            if (options.seedDemo() && DemoData.seed(authService, subscriptionService)) {
                console.println("Dados de demonstração criados em "
                        + dataDirectory.toAbsolutePath());
                console.println("E-mail: " + DemoData.DEMO_EMAIL
                        + " | Senha: " + DemoData.DEMO_PASSWORD);
            }

            new App(
                            console,
                            session,
                            authService,
                            subscriptionService,
                            dashboardService,
                            exportService)
                    .run();
        } catch (AppExitException exception) {
            console.println();
            console.println("Até logo!");
        } catch (IOException exception) {
            System.err.println("Erro de arquivo: " + exception.getMessage());
            System.exit(1);
        } catch (RuntimeException exception) {
            System.err.println("Erro inesperado: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("""
                JavaMinder - gerenciador de assinaturas por linha de comando

                Uso:
                  ./run.sh [opções]

                Opções:
                  -d, --data <dir>   diretório dos arquivos JSON (padrão: ./data)
                  --seed-demo        cria conta e assinaturas de exemplo se não houver usuários
                  -h, --help         mostra esta ajuda

                Arquivos gerados:
                  <dir>/users.json          usuários cadastrados
                  <dir>/subscriptions.json  assinaturas cadastradas
                  <dir>/exports/*.csv       exportações feitas pelo menu
                """);
    }
}
