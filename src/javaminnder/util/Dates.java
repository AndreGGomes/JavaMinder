package javaminnder.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Formatação e cálculos de datas no padrão brasileiro. */
public final class Dates {

    public static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Dates() {
    }

    public static String format(LocalDate date) {
        return date == null ? "-" : date.format(BR);
    }

    public static long daysUntil(LocalDate date) {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    public static String relative(LocalDate date) {
        long days = daysUntil(date);
        if (days == 0) {
            return "hoje";
        }
        if (days == 1) {
            return "amanhã";
        }
        if (days > 1) {
            return "em " + days + " dias";
        }
        return "há " + Math.abs(days) + " dias";
    }
}
