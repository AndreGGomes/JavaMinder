package javaminnder.model;

import java.util.Locale;

/** Usuário do JavaMinder. A senha nunca é guardada em texto puro: apenas o hash. */
public class User extends BaseEntity {

    private /*@ spec_public nullable @*/ String username;
    private /*@ spec_public nullable @*/ String email;
    private /*@ spec_public nullable @*/ String passwordHash;
    private /*@ spec_public nullable @*/ UserRole role;

    /*@ public behavior
      @*/
    public /*@ skipesc @*/ User(
            /*@ nullable @*/ String username,
            /*@ nullable @*/ String email,
            /*@ nullable @*/ String passwordHash,
            /*@ nullable @*/ UserRole role) {
        super();
        this.username = username;
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /*@ public normal_behavior
      @   ensures \result == username;
      @*/
    public /*@ pure nullable @*/ String getUsername() {
        return username;
    }

    /*@ public normal_behavior
      @   assignable this.username;
      @   ensures this.username == username;
      @*/
    public void setUsername(/*@ nullable @*/ String username) {
        this.username = username;
    }

    /*@ public normal_behavior
      @   ensures \result == email;
      @*/
    public /*@ pure nullable @*/ String getEmail() {
        return email;
    }

    /*@ public behavior
      @   assignable this.email;
      @*/
    public /*@ skipesc @*/ void setEmail(/*@ nullable @*/ String email) {
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    /*@ public normal_behavior
      @   ensures \result == passwordHash;
      @*/
    public /*@ pure nullable @*/ String getPasswordHash() {
        return passwordHash;
    }

    /*@ public normal_behavior
      @   assignable this.passwordHash;
      @   ensures this.passwordHash == passwordHash;
      @*/
    public void setPasswordHash(/*@ nullable @*/ String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /*@ public normal_behavior
      @   ensures \result == role;
      @*/
    public /*@ pure nullable @*/ UserRole getRole() {
        return role;
    }

    /*@ public normal_behavior
      @   assignable this.role;
      @   ensures this.role == role;
      @*/
    public void setRole(/*@ nullable @*/ UserRole role) {
        this.role = role;
    }

    /*@ public normal_behavior
      @   ensures \result == (role == UserRole.ADMIN);
      @*/
    public /*@ pure @*/ boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /*@ also
      @ public normal_behavior
      @   requires username != null;  
      @   ensures \result == username;
      @*/
    @Override
    public /*@ pure @*/ String label() {
        return username == null ? "" : username;
    }

    /** Linha usada nas listagens administrativas. */
    /*@ public normal_behavior
      @   requires role != null;
      @   ensures \result != null;
      @*/
    public /*@ pure @*/ String describe() {
        return username + " <" + email + "> - " + role.label();
    }
}
