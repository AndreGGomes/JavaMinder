package javaminnder;

import java.math.BigDecimal;
import java.time.LocalDate;
import javaminnder.model.BillingCycle;
import javaminnder.model.User;
import javaminnder.service.AuthService;
import javaminnder.service.SubscriptionService;

/** Cria uma conta e algumas assinaturas de exemplo, apenas quando o banco está vazio. */
public final class DemoData {

    public static final String DEMO_EMAIL = "demo@javaminnder.local";
    public static final String DEMO_PASSWORD = "JavaMinder123";

    private DemoData() {
    }

    /** @return {@code true} se os dados de demonstração foram realmente criados. */
    public static boolean seed(AuthService authService, SubscriptionService subscriptionService) {
        if (!authService.listUsers().isEmpty()) {
            return false;
        }
        User user = authService.register("Demo", DEMO_EMAIL, DEMO_PASSWORD);
        subscriptionService.add(user, "Netflix", new BigDecimal("44.90"),
                BillingCycle.MONTHLY, LocalDate.now().plusDays(7), "Plano padrão");
        subscriptionService.add(user, "Spotify", new BigDecimal("21.90"),
                BillingCycle.MONTHLY, LocalDate.now().plusDays(14), "Plano Duo");
        subscriptionService.add(user, "iCloud+", new BigDecimal("12.90"),
                BillingCycle.MONTHLY, LocalDate.now().plusDays(12), "200 GB");
        subscriptionService.add(user, "Adobe Creative Cloud", new BigDecimal("359.00"),
                BillingCycle.YEARLY, LocalDate.now().plusMonths(4), "Plano anual");
        return true;
    }
}
