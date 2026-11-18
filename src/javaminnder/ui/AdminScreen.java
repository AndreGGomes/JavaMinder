package javaminnder.ui;

import javaminnder.model.Subscription;
import javaminnder.model.User;
import javaminnder.service.AuthService;
import javaminnder.service.SubscriptionService;
import javaminnder.util.Dates;
import javaminnder.util.Money;

/** Visão administrativa: usuários cadastrados e todas as assinaturas. */
public class AdminScreen {

    private final Console console;
    private final AuthService authService;
    private final SubscriptionService subscriptionService;

    public AdminScreen(
            Console console,
            AuthService authService,
            SubscriptionService subscriptionService) {
        this.console = console;
        this.authService = authService;
        this.subscriptionService = subscriptionService;
    }

    public void show() {
        boolean running = true;
        while (running) {
            console.println(Menu.header("Administração"));
            console.println(Menu.option(1, "Listar usuários"));
            console.println(Menu.option(2, "Listar todas as assinaturas"));
            console.println(Menu.option(0, "Voltar ao painel"));
            console.println();
            int choice = console.readInt("  Opção: ", 0, 2);
            switch (choice) {
                case 1 -> listUsers();
                case 2 -> listSubscriptions();
                case 0 -> running = false;
                default -> {
                    // opção já validada
                }
            }
        }
    }

    private void listUsers() {
        console.println(Menu.header("Usuários cadastrados"));
        for (User user : authService.listUsers()) {
            console.println(Menu.message("#" + user.getId() + " " + user.describe()));
        }
    }

    private void listSubscriptions() {
        console.println(Menu.header("Todas as assinaturas"));
        for (Subscription subscription : subscriptionService.findAll()) {
            String owner = subscription.getUserId() == null
                    ? "?"
                    : authService.findById(subscription.getUserId())
                            .map(User::getEmail)
                            .orElse("?");
            console.println(Menu.message("#" + subscription.getId()
                    + " " + subscription.getName()
                    + " | " + owner
                    + " | " + Money.format(subscription.getPrice())
                    + " | próxima " + Dates.format(subscription.getNextBillingDate())));
        }
    }
}
