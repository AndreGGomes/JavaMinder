package javaminnder.ui;

import javaminnder.service.AuthService;
import javaminnder.service.DashboardService;
import javaminnder.service.ExportService;
import javaminnder.service.Session;
import javaminnder.service.SubscriptionService;

/** Monta as telas e alterna entre "deslogado" e "logado". */
public class App {

    private final AuthService authService;
    private final AuthScreen authScreen;
    private final MainMenuScreen mainMenuScreen;

    public App(
            Console console,
            Session session,
            AuthService authService,
            SubscriptionService subscriptionService,
            DashboardService dashboardService,
            ExportService exportService) {
        this.authService = authService;
        this.authScreen = new AuthScreen(console, authService);
        AdminScreen adminScreen = new AdminScreen(console, authService, subscriptionService);
        this.mainMenuScreen = new MainMenuScreen(
                console,
                authService,
                session,
                dashboardService,
                exportService,
                new SubscriptionScreen(console, session, subscriptionService),
                new ReportScreen(console, session, dashboardService),
                new AccountScreen(console, session, authService),
                adminScreen);
    }

    public void run() {
        while (true) {
            if (authService.isAuthenticated()) {
                mainMenuScreen.show();
            } else {
                authScreen.show();
            }
        }
    }
}
