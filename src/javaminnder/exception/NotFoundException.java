package javaminnder.exception;

/** Registro pedido não existe ou não pertence ao usuário logado. */
public class NotFoundException extends AppException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
