package javaminnder.util;

import java.util.regex.Pattern;
import javaminnder.exception.ValidationException;

/** Regras de validação de entrada compartilhadas pelos serviços. */
public final class Validators {

    private static final Pattern EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private static final int MIN_PASSWORD_LENGTH = 8;

    /*@ private normal_behavior
      @   ensures true;
      @*/
    private Validators() {
    }

    /*@ public behavior
      @   requires minLength >= 0;
      @   requires maxLength >= minLength;
      @*/
    public static String requireText(String value, String field, int minLength, int maxLength) {
        String clean = value == null ? "" : value.trim();
        if (clean.length() < minLength) {
            throw new ValidationException(
                    "O campo " + field + " precisa de pelo menos " + minLength + " caractere(s).");
        }
        if (clean.length() > maxLength) {
            throw new ValidationException(
                    "O campo " + field + " aceita no máximo " + maxLength + " caracteres.");
        }
        return clean;
    }

    /*@ public behavior
      @   assignable \nothing;
      @   ensures \result != null;
      @*/
    /*@ skipesc @*/
    public static String requireEmail(String value) {
        String clean = value == null ? "" : value.trim().toLowerCase();
        if (clean.isEmpty()) {
            throw new ValidationException("Informe um e-mail.");
        }
        if (!EMAIL.matcher(clean).matches()) {
            throw new ValidationException("E-mail inválido: " + value);
        }
        return clean;
    }

    /*@ public behavior
      @*/
    public static String requirePassword(String value) {
        if (value == null || value.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException(
                    "A senha precisa ter pelo menos " + MIN_PASSWORD_LENGTH + " caracteres.");
        }
        if (value.isBlank()) {
            throw new ValidationException("A senha não pode ser composta apenas de espaços.");
        }
        return value;
    }
}