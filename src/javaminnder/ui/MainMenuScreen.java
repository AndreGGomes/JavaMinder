package javaminnder.ui;

import java.nio.file.Path;
import javaminnder.model.User;
import javaminnder.service.AuthService;
import javaminnder.service.DashboardService;
import javaminnder.service.ExportService;
import javaminnder.service.Session;
import javaminnder.util.Dates;
import javaminnder.util.Money;

/** Painel principal: resumo dos gastos e acesso às demais telas. */
public class MainMenuScreen {

    private final Console console;
    private final AuthService authService;
    private final Session session;
    private final DashboardService dashboard;
    private final ExportService exportService;
    private final SubscriptionScreen subscriptionScreen;
    private final ReportScreen reportScreen;
    private final AccountScreen accountScreen;
    private final AdminScreen adminScreen;

    public MainMenuScreen(
            Console console,
            AuthService authService,
            Session session,
            DashboardService dashboard,
            ExportService exportService,
            SubscriptionScreen subscriptionScreen,
            ReportScreen reportScreen,
            AccountScreen accountScreen,
            AdminScreen adminScreen) {
        this.console = console;
        this.authService = authService;
        this.session = session;
        this.dashboard = dashboard;
        this.exportService = exportService;
        this.subscriptionScreen = subscriptionScreen;
        this.reportScreen = reportScreen;
        this.accountScreen = accountScreen;
        this.adminScreen = adminScreen;
    }

    public void show() {
        while (authService.isAuthenticated()) {
            User user = session.currentUser();
            console.println(Menu.header("JavaMinder - Painel"));
            printDashboard(user);

            console.println(Menu.option(1, "Minhas assinaturas"));
            console.println(Menu.option(2, "Relatórios"));
            console.println(Menu.option(3, "Exportar assinaturas para CSV"));
            console.println(Menu.option(4, "Minha conta"));
            if (user.isAdmin()) {
                console.println(Menu.option(5, "Administração"));
            }
            console.println(Menu.option(0, "Sair da conta"));
            console.println();

            int choice = console.readInt("  Opção: ", 0, user.isAdmin() ? 5 : 4);
            switch (choice) {
                case 1 -> subscriptionScreen.show();
                case 2 -> reportScreen.show();
                case 3 -> export(user);
                case 4 -> accountScreen.show();
                case 5 -> adminScreen.show();
                case 0 -> {
                    authService.logout();
                    console.println(Menu.message(
                            "Sessão encerrada. Até logo, " + user.getUsername() + "!"));
                }
                default -> {
                    // opção já validada
                }
            }
        }
    }

    private void printDashboard(User user) {
        console.println(Menu.message("Olá, " + user.getUsername() + "!"));
        console.println(Menu.message(
                "Gasto mensal estimado : " + Money.format(dashboard.monthlyTotal(user))));
        console.println(Menu.message(
                "Gasto anual estimado  : " + Money.format(dashboard.yearlyTotal(user))));
        console.println(Menu.message("Assinaturas ativas    : " + dashboard.billableCount(user)
                + " de " + dashboard.totalCount(user)));
        dashboard.nextBilling(user).ifPresentOrElse(
                subscription -> console.println(Menu.message("Próxima cobrança      : "
                        + subscription.getName()
                        + " em " + Dates.format(subscription.getNextBillingDate())
                        + " (" + Dates.relative(subscription.getNextBillingDate()) + ")")),
                () -> console.println(Menu.message("Próxima cobrança      : nenhuma cadastrada")));
        console.println();
    }

    private void export(User user) {
        try {
            Path file = exportService.exportCsv(user);
            console.println(Menu.message("Arquivo gerado: " + file.toAbsolutePath()));
        } catch (RuntimeException exception) {
            console.println(Menu.error(exception.getMessage()));
        }
    }
}
