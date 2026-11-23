package javaminnder.model;

/** Papel do usuário no sistema. O primeiro usuário cadastrado se torna ADMIN. */
public enum UserRole implements Describable {

    USER("Usuário"),
    ADMIN("Administrador");

    private final /*@ spec_public non_null @*/ String label;

    /*@ behavior
      @   requires label != null;
      @   ensures this.label == label;
      @*/
    UserRole(String label) {
        this.label = label;
    }

    /*@ also
      @ public normal_behavior
      @   ensures \result == label;
      @   ensures \result != null;
      @*/
    @Override
    public /*@ pure @*/ String label() {
        return label;
    }
}
