package javaminnder.exception;

/** Dados informados pelo usuário não atendem às regras de validação. */
public class ValidationException extends AppException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
