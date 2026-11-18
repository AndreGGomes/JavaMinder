package javaminnder.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Assinatura de um serviço, pertencente a um usuário. */
public class Subscription extends BaseEntity {

    private Long userId;
    private String name;
    private BigDecimal price;
    private BillingCycle cycle;
    private SubscriptionStatus status;
    private LocalDate nextBillingDate;
    private String notes;

    public Subscription(
            Long userId,
            String name,
            BigDecimal price,
            BillingCycle cycle,
            SubscriptionStatus status,
            LocalDate nextBillingDate,
            String notes) {
        super();
        this.userId = userId;
        this.name = name;
        this.price = price;
        this.cycle = cycle;
        this.status = status;
        this.nextBillingDate = nextBillingDate;
        this.notes = notes == null ? "" : notes;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BillingCycle getCycle() {
        return cycle;
    }

    public void setCycle(BillingCycle cycle) {
        this.cycle = cycle;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDate getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes;
    }

    public boolean isBillable() {
        return status != null && status.isBillable();
    }

    /** Valor mensal que esta assinatura representa nos totais. */
    public BigDecimal monthlyEquivalent() {
        return isBillable() ? cycle.monthlyEquivalent(price) : BigDecimal.ZERO.setScale(2);
    }

    /** Valor anual que esta assinatura representa nos totais. */
    public BigDecimal yearlyEquivalent() {
        return isBillable() ? cycle.yearlyEquivalent(price) : BigDecimal.ZERO.setScale(2);
    }

    /** Registra o pagamento do ciclo atual, avançando a próxima cobrança. */
    public LocalDate renew() {
        this.nextBillingDate = cycle.advance(nextBillingDate);
        touch();
        return nextBillingDate;
    }

    @Override
    public String label() {
        return name;
    }

    @Override
    public String toString() {
        return "#" + getId() + " " + name;
    }
}
