package javaminnder.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Formatação monetária em real, sempre com BigDecimal. */
public final class Money {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Money() {
    }

    public static String format(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        return NumberFormat.getCurrencyInstance(PT_BR).format(safe);
    }

    /** Valor sem símbolo de moeda, com duas casas e vírgula decimal. */
    public static String plain(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        return String.format(PT_BR, "%.2f", safe);
    }
}
