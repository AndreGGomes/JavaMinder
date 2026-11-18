package javaminnder.storage;

/** Erro de sintaxe ou de tipo ao ler/gravar JSON. */
public class JsonSyntaxException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JsonSyntaxException(String message) {
        super(message);
    }
}
