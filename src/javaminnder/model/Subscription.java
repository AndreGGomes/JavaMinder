package javaminnder.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Assinatura de um serviço, pertencente a um usuário. */
public class Subscription extends BaseEntity {

    private /*@ spec_public nullable @*/ Long userId;
    private /*@ spec_public nullable @*/ String name;
    private /*@ spec_public nullable @*/ BigDecimal price;
    private /*@ spec_public nullable @*/ BillingCycle cycle;
    private /*@ spec_public nullable @*/ SubscriptionStatus status;
    private /*@ spec_public nullable @*/ LocalDate nextBillingDate;
    private /*@ spec_public nullable @*/ String notes;

    /*@ public behavior
      @*/
    public Subscription(
            /*@ nullable @*/ Long userId,
            /*@ nullable @*/ String name,
            /*@ nullable @*/ BigDecimal price,
            /*@ nullable @*/ BillingCycle cycle,
            /*@ nullable @*/ SubscriptionStatus status,
            /*@ nullable @*/ LocalDate nextBillingDate,
            /*@ nullable @*/ String notes) {
        super();
        this.userId = userId;
        this.name = name;
        this.price = price;
        this.cycle = cycle;
        this.status = status;
        this.nextBillingDate = nextBillingDate;
        this.notes = notes == null ? "" : notes;
    }

    /*@ public normal_behavior
      @   ensures \result == userId;
      @*/
    public /*@ pure nullable @*/ Long getUserId() {
        return userId;
    }

    /*@ public normal_behavior
      @   assignable this.userId;
      @   ensures this.userId == userId;
      @*/
    public void setUserId(/*@ nullable @*/ Long userId) {
        this.userId = userId;
    }

    /*@ public normal_behavior
      @   ensures \result == name;
      @*/
    public /*@ pure nullable @*/ String getName() {
        return name;
    }

    /*@ public normal_behavior
      @   assignable this.name;
      @   ensures this.name == name;
      @*/
    public void setName(/*@ nullable @*/ String name) {
        this.name = name;
    }

    /*@ public normal_behavior
      @   ensures \result == price;
      @*/
    public /*@ pure nullable @*/ BigDecimal getPrice() {
        return price;
    }

    /*@ public normal_behavior
      @   assignable this.price;
      @   ensures this.price == price;
      @*/
    public void setPrice(/*@ nullable @*/ BigDecimal price) {
        this.price = price;
    }

    /*@ public normal_behavior
      @   ensures \result == cycle;
      @*/
    public /*@ pure nullable @*/ BillingCycle getCycle() {
        return cycle;
    }

    /*@ public normal_behavior
      @   assignable this.cycle;
      @   ensures this.cycle == cycle;
      @*/
    public void setCycle(/*@ nullable @*/ BillingCycle cycle) {
        this.cycle = cycle;
    }

    /*@ public normal_behavior
      @   ensures \result == status;
      @*/
    public /*@ pure nullable @*/ SubscriptionStatus getStatus() {
        return status;
    }

    /*@ public normal_behavior
      @   assignable this.status;
      @   ensures this.status == status;
      @*/
    public void setStatus(/*@ nullable @*/ SubscriptionStatus status) {
        this.status = status;
    }

    /*@ public normal_behavior
      @   assignable \nothing;
      @   ensures \result != null;
      @*/
    public /*@ skipesc @*/ LocalDate advance(/*@ nullable @*/ LocalDate date) {
        return nextBillingDate;
    }

    /*@ public normal_behavior
      @   ensures \result == nextBillingDate;
      @*/
    public /*@ pure nullable @*/ LocalDate getNextBillingDate() {
        return nextBillingDate;
    }

    /*@ public normal_behavior
      @   assignable this.nextBillingDate;
      @   ensures this.nextBillingDate == nextBillingDate;
      @*/
    public void setNextBillingDate(/*@ nullable @*/ LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    /*@ public normal_behavior
      @   ensures \result == notes;
      @*/
    public /*@ pure nullable @*/ String getNotes() {
        return notes;
    }

    /*@ public normal_behavior
      @   assignable this.notes;
      @   ensures this.notes != null;
      @   ensures (notes == null) ==> this.notes.equals("");
      @   ensures (notes != null) ==> this.notes == notes;
      @*/
    public void setNotes(/*@ nullable @*/ String notes) {
        this.notes = notes == null ? "" : notes;
    }

    /*@ public normal_behavior
      @   ensures \result == (status != null && status.isBillable());
      @*/
    public /*@ pure @*/ boolean isBillable() {
        return status != null && status.isBillable();
    }

    /** Valor mensal que esta assinatura representa nos totais. */
    /*@ public behavior
      @*/
    public /*@ skipesc @*/ BigDecimal monthlyEquivalent() {
        return isBillable() ? cycle.monthlyEquivalent(price) : BigDecimal.ZERO.setScale(2);
    }

    /** Valor anual que esta assinatura representa nos totais. */
    /*@ public behavior
      @*/
    public /*@ skipesc @*/ BigDecimal yearlyEquivalent() {
        return isBillable() ? cycle.yearlyEquivalent(price) : BigDecimal.ZERO.setScale(2);
    }

    /** Registra o pagamento do ciclo atual, avançando a próxima cobrança. */
    /*@ public normal_behavior
      @   requires cycle != null;
      @   assignable this.nextBillingDate, updatedAt;
      @   ensures \result == this.nextBillingDate;
      @*/
    public /*@ nullable @*/ LocalDate renew() {
        this.nextBillingDate = cycle.advance(nextBillingDate);
        touch();
        return nextBillingDate;
    }

    /*@ also
      @ public normal_behavior
      @   requires name != null;
      @   ensures \result == name;
      @   ensures \result != null;
      @*/
    @Override
    public /*@ pure @*/ String label() {
        return name;
    }

    /*@ also
      @ public normal_behavior
      @   ensures \result != null;
      @*/
    @Override
    public /*@ pure @*/ String toString() {
        return "#" + getId() + " " + name;
    }
}
