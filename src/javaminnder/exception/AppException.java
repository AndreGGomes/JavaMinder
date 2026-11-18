package javaminnder.exception;

/** Superclasse de todos os erros de negócio do JavaMinder. */
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AppException(String message) {
        super(message);
    }
}
