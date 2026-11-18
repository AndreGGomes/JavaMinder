package javaminnder.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javaminnder.model.BillingCycle;
import javaminnder.model.Subscription;
import javaminnder.model.User;

/** Agregações usadas pelo painel e pelos relatórios. */
public class DashboardService {

    private final SubscriptionService subscriptions;

    public DashboardService(SubscriptionService subscriptions) {
        this.subscriptions = subscriptions;
    }

    public BigDecimal monthlyTotal(User user) {
        return total(user, Subscription::monthlyEquivalent);
    }

    public BigDecimal yearlyTotal(User user) {
        return total(user, Subscription::yearlyEquivalent);
    }

    public long billableCount(User user) {
        return subscriptions.listBillableFor(user).size();
    }

    public long totalCount(User user) {
        return subscriptions.listFor(user).size();
    }

    public Optional<Subscription> nextBilling(User user) {
        LocalDate today = LocalDate.now();
        return subscriptions.listBillableFor(user).stream()
                .filter(subscription -> !subscription.getNextBillingDate().isBefore(today))
                .min(Comparator.comparing(Subscription::getNextBillingDate));
    }

    /** Cobranças previstas nos próximos {@code days} dias. */
    public List<Subscription> upcoming(User user, int days) {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(days);
        return subscriptions.listBillableFor(user).stream()
                .filter(subscription -> !subscription.getNextBillingDate().isBefore(today)
                        && !subscription.getNextBillingDate().isAfter(limit))
                .sorted(Comparator.comparing(Subscription::getNextBillingDate))
                .collect(Collectors.toList());
    }

    public List<Subscription> topExpenses(User user, int limit) {
        return subscriptions.listBillableFor(user).stream()
                .sorted(Comparator.comparing(Subscription::monthlyEquivalent).reversed())
                .limit(Math.max(limit, 1))
                .collect(Collectors.toList());
    }

    /** Total mensal agrupado por ciclo de cobrança. */
    public Map<BillingCycle, BigDecimal> totalsByCycle(User user) {
        Map<BillingCycle, BigDecimal> totals = new EnumMap<>(BillingCycle.class);
        for (BillingCycle cycle : BillingCycle.values()) {
            totals.put(cycle, BigDecimal.ZERO.setScale(2));
        }
        for (Subscription subscription : subscriptions.listBillableFor(user)) {
            totals.merge(
                    subscription.getCycle(),
                    subscription.monthlyEquivalent(),
                    BigDecimal::add);
        }
        return totals;
    }

    private BigDecimal total(User user, java.util.function.Function<Subscription, BigDecimal> extractor) {
        return subscriptions.listBillableFor(user).stream()
                .map(extractor)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
