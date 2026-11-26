package javaminnder.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Ciclo de cobrança de uma assinatura, com as conversões de valor que ele implica. */
public enum BillingCycle implements Describable {

    MONTHLY("Mensal", 1),
    YEARLY("Anual", 12);

    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);

    private final /*@ spec_public non_null @*/ String label;
    private final /*@ spec_public @*/ int months;

    /*@ public invariant 0 < months && months <= 12;
      @*/

    /*@ behavior
      @   requires label != null;
      @   requires 0 < months && months <= 12;
      @   ensures this.label == label;
      @   ensures this.months == months;
      @*/
    BillingCycle(String label, int months) {
        this.label = label;
        this.months = months;
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
      @   ensures \result == months;
      @   ensures \result > 0 && \result <= 12;
      @*/
    public /*@ pure @*/ int months() {
        return months;
    }

    /** Valor equivalente por mês (planos anuais são divididos por 12). */
    /*@ public behavior
      @   requires price != null;
      @   ensures \result != null;
      @*/
    public BigDecimal monthlyEquivalent(BigDecimal price) {
        return price.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }

    /** Valor equivalente por ano (planos mensais são projetados para 12 meses). */
    /*@ public behavior
      @   requires price != null;
      @   ensures \result != null;
      @*/
    public BigDecimal yearlyEquivalent(BigDecimal price) {
        return price.multiply(MONTHS_IN_YEAR)
                .divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }

    /** Avança a data de cobrança em um ciclo completo. */
    /*@ public behavior
      @   requires date != null;
      @   assignable \nothing;
      @   ensures \result != null;
      @*/
    /*@ skipesc @*/
    public LocalDate advance(LocalDate date) {
        return date.plusMonths(months);
    }
}
