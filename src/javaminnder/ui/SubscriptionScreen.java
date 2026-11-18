package javaminnder.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javaminnder.exception.AppException;
import javaminnder.model.BillingCycle;
import javaminnder.model.Subscription;
import javaminnder.model.SubscriptionStatus;
import javaminnder.model.User;
import javaminnder.service.Session;
import javaminnder.service.SubscriptionService;
import javaminnder.util.Dates;
import javaminnder.util.Money;

/** Tela de gerenciamento das assinaturas do usuário logado. */
public class SubscriptionScreen {

    private final Console console;
    private final Session session;
    private final SubscriptionService subscriptionService;

    public SubscriptionScreen(
            Console console, Session session, SubscriptionService subscriptionService) {
        this.console = console;
        this.session = session;
        this.subscriptionService = subscriptionService;
    }

    public void show() {
        boolean running = true;
        while (running) {
            console.println(Menu.header("Minhas assinaturas"));
            console.println(Menu.option(1, "Listar todas"));
            console.println(Menu.option(2, "Cadastrar assinatura"));
            console.println(Menu.option(3, "Editar assinatura"));
            console.println(Menu.option(4, "Remover assinatura"));
            console.println(Menu.option(5, "Buscar por nome ou observação"));
            console.println(Menu.option(6, "Registrar pagamento (avançar cobrança)"));
            console.println(Menu.option(0, "Voltar ao painel"));
            console.println();
            int choice = console.readInt("  Opção: ", 0, 6);
            try {
                switch (choice) {
                    case 1 -> printList(subscriptionService.listFor(user()));
                    case 2 -> create();
                    case 3 -> edit();
                    case 4 -> remove();
                    case 5 -> search();
                    case 6 -> registerPayment();
                    case 0 -> running = false;
                    default -> {
                        // opção já validada
                    }
                }
            } catch (AppException exception) {
                console.println(Menu.error(exception.getMessage()));
            }
        }
    }

    private User user() {
        return session.currentUser();
    }

    private void create() {
        console.println(Menu.message("Nova assinatura"));
        String name = console.readLine("  Serviço: ");
        BigDecimal price = console.readMoney("  Preço: ");
        BillingCycle cycle = chooseCycle();
        LocalDate nextBillingDate = console.readDate("  Próxima cobrança (dd/MM/aaaa): ");
        String notes = console.readLine("  Observações (opcional): ");
        Subscription created =
                subscriptionService.add(user(), name, price, cycle, nextBillingDate, notes);
        console.println(Menu.message(
                "Assinatura #" + created.getId() + " cadastrada: " + created.getName()));
    }

    private void edit() {
        List<Subscription> items = subscriptionService.listFor(user());
        if (items.isEmpty()) {
            console.println(Menu.message("Nenhuma assinatura cadastrada."));
            return;
        }
        printList(items);
        long id = console.readLong("  ID da assinatura: ");
        Subscription current = subscriptionService.require(user(), id);

        String name = current.getName();
        BigDecimal price = current.getPrice();
        BillingCycle cycle = current.getCycle();
        LocalDate nextBillingDate = current.getNextBillingDate();
        SubscriptionStatus status = current.getStatus();
        String notes = current.getNotes();

        while (true) {
            console.println(Menu.header("Editando #" + id + " - " + name));
            console.println(Menu.option(1, "Nome: " + name));
            console.println(Menu.option(2, "Preço: " + Money.format(price)));
            console.println(Menu.option(3, "Ciclo: " + cycle.label()));
            console.println(Menu.option(4, "Próxima cobrança: " + Dates.format(nextBillingDate)));
            console.println(Menu.option(5, "Status: " + status.label()));
            console.println(Menu.option(6, "Observações: " + (notes.isBlank() ? "(vazio)" : notes)));
            console.println(Menu.option(0, "Salvar e voltar"));
            console.println();
            int choice = console.readInt("  Opção: ", 0, 6);
            if (choice == 0) {
                try {
                    subscriptionService.update(
                            user(), id, name, price, cycle, nextBillingDate, status, notes);
                    console.println(Menu.message("Assinatura atualizada."));
                } catch (AppException exception) {
                    console.println(Menu.error(exception.getMessage()));
                }
                return;
            }
            switch (choice) {
                case 1 -> name = console.readLine("  Novo nome: ");
                case 2 -> price = console.readMoney("  Novo preço: ");
                case 3 -> cycle = chooseCycle();
                case 4 -> nextBillingDate = console.readDate("  Nova data (dd/MM/aaaa): ");
                case 5 -> status = chooseStatus();
                case 6 -> notes = console.readLine("  Observações: ");
                default -> {
                    // opção já validada
                }
            }
        }
    }

    private void remove() {
        List<Subscription> items = subscriptionService.listFor(user());
        if (items.isEmpty()) {
            console.println(Menu.message("Nenhuma assinatura cadastrada."));
            return;
        }
        printList(items);
        long id = console.readLong("  ID da assinatura: ");
        Subscription subscription = subscriptionService.require(user(), id);
        if (!console.confirm("  Remover \"" + subscription.getName() + "\"?")) {
            console.println(Menu.message("Operação cancelada."));
            return;
        }
        subscriptionService.delete(user(), id);
        console.println(Menu.message("Assinatura removida."));
    }

    private void search() {
        String term = console.readLine("  Termo de busca: ");
        printList(subscriptionService.search(user(), term));
    }

    private void registerPayment() {
        List<Subscription> billable = subscriptionService.listBillableFor(user());
        if (billable.isEmpty()) {
            console.println(Menu.message(
                    "Nenhuma assinatura ativa para registrar pagamento."));
            return;
        }
        printList(billable);
        long id = console.readLong("  ID da assinatura paga: ");
        Subscription updated = subscriptionService.registerPayment(user(), id);
        console.println(Menu.message("Pagamento registrado. Próxima cobrança: "
                + Dates.format(updated.getNextBillingDate())));
    }

    private BillingCycle chooseCycle() {
        BillingCycle[] cycles = BillingCycle.values();
        console.println(Menu.message("Ciclo de cobrança:"));
        for (int index = 0; index < cycles.length; index++) {
            console.println(Menu.option(index + 1, cycles[index].label()));
        }
        int choice = console.readInt("  Ciclo: ", 1, cycles.length);
        return cycles[choice - 1];
    }

    private SubscriptionStatus chooseStatus() {
        SubscriptionStatus[] statuses = SubscriptionStatus.values();
        console.println(Menu.message("Status:"));
        for (int index = 0; index < statuses.length; index++) {
            console.println(Menu.option(index + 1, statuses[index].label()));
        }
        int choice = console.readInt("  Status: ", 1, statuses.length);
        return statuses[choice - 1];
    }

    private void printList(List<Subscription> items) {
        if (items.isEmpty()) {
            console.println(Menu.message("Nenhuma assinatura encontrada."));
            return;
        }
        console.println();
        console.println(String.format(
                "  %-4s %-22s %10s %-8s %-10s %-12s %10s",
                "ID", "SERVIÇO", "PREÇO", "CICLO", "STATUS", "PRÓXIMA", "MENSAL"));
        console.println("  " + "-".repeat(84));
        for (Subscription subscription : items) {
            console.println(String.format(
                    "  %-4d %-22.22s %10s %-8.8s %-10.10s %-12s %10s",
                    subscription.getId(),
                    subscription.getName(),
                    Money.plain(subscription.getPrice()),
                    subscription.getCycle().label(),
                    subscription.getStatus().label(),
                    Dates.format(subscription.getNextBillingDate()),
                    Money.plain(subscription.monthlyEquivalent())));
        }
        console.println();
    }
}
