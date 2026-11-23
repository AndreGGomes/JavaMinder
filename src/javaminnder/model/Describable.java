package javaminnder.model;

/**
 * Contrato implementado por todo elemento do domínio que pode ser exibido ao usuário
 * com um rótulo legível. É a base do polimorfismo de exibição do JavaMinder.
 */
public interface Describable {

    /** Rótulo curto, em português, usado nos menus e relatórios. */
    /*@ public normal_behavior
      @   ensures \result != null;
      @*/
    String label();
}
