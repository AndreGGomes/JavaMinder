package javaminnder.ui;

/** Sinaliza que o usuário pediu para encerrar o programa (ou que a entrada acabou). */
public class AppExitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AppExitException(String message) {
        super(message);
    }
}
