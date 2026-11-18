package javaminnder.repository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javaminnder.model.BillingCycle;
import javaminnder.model.Subscription;
import javaminnder.model.SubscriptionStatus;
import javaminnder.storage.Json;

/** Persiste assinaturas em {@code subscriptions.json}. */
public class SubscriptionRepository extends AbstractJsonRepository<Subscription> {

    public SubscriptionRepository(Path file) {
        super(file);
    }

    public List<Subscription> findByUserId(long userId) {
        return findAll().stream()
                .filter(subscription -> subscription.getUserId() != null
                        && subscription.getUserId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    protected String entityName() {
        return "Assinatura";
    }

    @Override
    protected Subscription fromJson(Map<String, Object> data) {
        SubscriptionStatus status = SubscriptionStatus.valueOf(
                Json.getString(data, "status") == null
                        ? SubscriptionStatus.ACTIVE.name()
                        : Json.getString(data, "status"));
        Subscription subscription = new Subscription(
                Json.getLong(data, "userId", 0L),
                Json.getString(data, "name"),
                new BigDecimal(Json.getString(data, "price")),
                BillingCycle.valueOf(Json.getString(data, "cycle")),
                status,
                dateValue(data, "nextBillingDate"),
                Json.getString(data, "notes"));
        subscription.setId(Json.getLong(data, "id", 0L));
        subscription.setCreatedAt(dateTimeValue(data, "createdAt"));
        subscription.setUpdatedAt(dateTimeValue(data, "updatedAt"));
        return subscription;
    }

    @Override
    protected Map<String, Object> toJson(Subscription subscription) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", subscription.getId());
        data.put("userId", subscription.getUserId());
        data.put("name", subscription.getName());
        data.put("price", subscription.getPrice().toPlainString());
        data.put("cycle", subscription.getCycle().name());
        data.put("status", subscription.getStatus().name());
        data.put("nextBillingDate", String.valueOf(subscription.getNextBillingDate()));
        data.put("notes", subscription.getNotes());
        data.put("createdAt", String.valueOf(subscription.getCreatedAt()));
        data.put("updatedAt", String.valueOf(subscription.getUpdatedAt()));
        return data;
    }
}
