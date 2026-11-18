package javaminnder.model;

/** Papel do usuário no sistema. O primeiro usuário cadastrado se torna ADMIN. */
public enum UserRole implements Describable {

    USER("Usuário"),
    ADMIN("Administrador");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}
