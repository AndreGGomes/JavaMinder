package javaminnder.service;

import javaminnder.exception.AuthException;
import javaminnder.model.User;

/** Sessão em memória: quem está logado neste processo do JavaMinder. */
public class Session {

    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public User currentUser() {
        if (currentUser == null) {
            throw new AuthException("Nenhum usuário autenticado. Faça login para continuar.");
        }
        return currentUser;
    }
}
