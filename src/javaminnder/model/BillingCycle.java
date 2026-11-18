package javaminnder.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Ciclo de cobrança de uma assinatura, com as conversões de valor que ele implica. */
public enum BillingCycle implements Describable {

    MONTHLY("Mensal", 1),
    YEARLY("Anual", 12);

    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);

    private final String label;
    private final int months;

    BillingCycle(String label, int months) {
        this.label = label;
        this.months = months;
    }

    @Override
    public String label() {
        return label;
    }

    public int months() {
        return months;
    }

    /** Valor equivalente por mês (planos anuais são divididos por 12). */
    public BigDecimal monthlyEquivalent(BigDecimal price) {
        return price.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }

    /** Valor equivalente por ano (planos mensais são projetados para 12 meses). */
    public BigDecimal yearlyEquivalent(BigDecimal price) {
        return price.multiply(MONTHS_IN_YEAR)
                .divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }

    /** Avança a data de cobrança em um ciclo completo. */
    public LocalDate advance(LocalDate date) {
        return date.plusMonths(months);
    }
}
