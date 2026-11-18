package javaminnder.exception;

/** Falha de autenticação: credenciais inválidas ou sessão ausente. */
public class AuthException extends AppException {

    private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(message);
    }
}
