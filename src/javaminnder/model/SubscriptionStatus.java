package javaminnder.model;

/** Situação da assinatura. Apenas as ativas entram nos totais de gasto. */
public enum SubscriptionStatus implements Describable {

    ACTIVE("Ativa", true),
    PAUSED("Pausada", false),
    CANCELED("Cancelada", false);

    private final String label;
    private final boolean billable;

    SubscriptionStatus(String label, boolean billable) {
        this.label = label;
        this.billable = billable;
    }

    @Override
    public String label() {
        return label;
    }

    public boolean isBillable() {
        return billable;
    }
}
