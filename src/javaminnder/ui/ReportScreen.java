package javaminnder.ui;

import java.util.List;
import javaminnder.model.Subscription;
import javaminnder.model.User;
import javaminnder.service.DashboardService;
import javaminnder.service.Session;
import javaminnder.util.Dates;
import javaminnder.util.Money;

/** Relatórios de gasto: resumo, próximas cobranças e maiores despesas. */
public class ReportScreen {

    private final Console console;
    private final Session session;
    private final DashboardService dashboard;

    public ReportScreen(Console console, Session session, DashboardService dashboard) {
        this.console = console;
        this.session = session;
        this.dashboard = dashboard;
    }

    public void show() {
        boolean running = true;
        while (running) {
            console.println(Menu.header("Relatórios"));
            console.println(Menu.option(1, "Resumo geral"));
            console.println(Menu.option(2, "Próximas cobranças (30 dias)"));
            console.println(Menu.option(3, "Top 5 maiores gastos"));
            console.println(Menu.option(0, "Voltar ao painel"));
            console.println();
            int choice = console.readInt("  Opção: ", 0, 3);
            switch (choice) {
                case 1 -> summary();
                case 2 -> upcoming();
                case 3 -> topExpenses();
                case 0 -> running = false;
                default -> {
                    // opção já validada
                }
            }
        }
    }

    private void summary() {
        User user = session.currentUser();
        console.println(Menu.header("Resumo de " + user.getUsername()));
        console.println(Menu.message("Assinaturas ativas ....: " + dashboard.billableCount(user)));
        console.println(Menu.message("Assinaturas cadastradas: " + dashboard.totalCount(user)));
        console.println(Menu.message(
                "Gasto mensal estimado .: " + Money.format(dashboard.monthlyTotal(user))));
        console.println(Menu.message(
                "Gasto anual estimado ..: " + Money.format(dashboard.yearlyTotal(user))));
        console.println();
        console.println(Menu.message("Gasto mensal por ciclo:"));
        dashboard.totalsByCycle(user).forEach((cycle, total) -> console.println(
                Menu.message("  " + String.format("%-8s", cycle.label()) + " "
                        + Money.format(total) + "/mês")));
    }

    private void upcoming() {
        User user = session.currentUser();
        List<Subscription> items = dashboard.upcoming(user, 30);
        console.println(Menu.header("Cobranças nos próximos 30 dias"));
        if (items.isEmpty()) {
            console.println(Menu.message("Nenhuma cobrança prevista nesse período."));
            return;
        }
        for (Subscription subscription : items) {
            console.println(Menu.message(
                    Dates.format(subscription.getNextBillingDate())
                            + " (" + Dates.relative(subscription.getNextBillingDate()) + ")"
                            + " - " + subscription.getName()
                            + " - " + Money.format(subscription.getPrice())));
        }
    }

    private void topExpenses() {
        User user = session.currentUser();
        List<Subscription> items = dashboard.topExpenses(user, 5);
        console.println(Menu.header("Maiores gastos mensais"));
        if (items.isEmpty()) {
            console.println(Menu.message("Nenhuma assinatura ativa."));
            return;
        }
        int position = 1;
        for (Subscription subscription : items) {
            console.println(Menu.message(position++ + ". " + subscription.getName()
                    + " - " + Money.format(subscription.monthlyEquivalent()) + "/mês ("
                    + subscription.getCycle().label() + ")"));
        }
    }
}
