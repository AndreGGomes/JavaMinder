package javaminnder.service;

import javaminnder.exception.AuthException;
import javaminnder.model.User;

/** Sessão em memória: quem está logado neste processo do JavaMinder. */
public class Session {

    private /*@ spec_public nullable @*/ User currentUser;

    /*@ public normal_behavior
      @   requires user != null;
      @   assignable this.currentUser;
      @   ensures currentUser == user;
      @*/
    public void login(User user) {
        this.currentUser = user;
    }

    /*@ public normal_behavior
      @   ensures currentUser == null;
      @*/
    public void logout() {
        this.currentUser = null;
    }

    /*@ public normal_behavior
      @   ensures \result <==> currentUser != null;
      @*/
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    /*@ public normal_behavior
      @   requires currentUser != null;
      @   ensures \result == currentUser;
      @ also
      @ public exceptional_behavior
      @   requires currentUser == null;
      @   signals (AuthException e) true;
      @*/
    public User currentUser() {
        if (currentUser == null) {
            throw new AuthException("Nenhum usuário autenticado. Faça login para continuar.");
        }
        return currentUser;
    }
}
