package javaminnder.model;

/** Situação da assinatura. Apenas as ativas entram nos totais de gasto. */
public enum SubscriptionStatus implements Describable {

    ACTIVE("Ativa", true),
    PAUSED("Pausada", false),
    CANCELED("Cancelada", false);

    private final /*@ spec_public non_null @*/ String label;
    private final /*@ spec_public @*/ boolean billable;

    /*@ behavior
      @   requires label != null;
      @   ensures this.label == label;
      @   ensures this.billable == billable;
      @*/
    SubscriptionStatus(String label, boolean billable) {
        this.label = label;
        this.billable = billable;
    }

    /*@ also
      @ public normal_behavior
      @   ensures \result == label;
      @   ensures \result != null;
      @*/
    @Override
    public /*@ pure @*/ String label() {
        return label;
    }

    /*@ public normal_behavior
      @   ensures \result == billable;
      @*/
    public /*@ pure @*/ boolean isBillable() {
        return billable;
    }
}
