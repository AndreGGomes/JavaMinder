package javaminnder.model;

import java.util.Locale;

/** Usuário do JavaMinder. A senha nunca é guardada em texto puro: apenas o hash. */
public class User extends BaseEntity {

    private String username;
    private String email;
    private String passwordHash;
    private UserRole role;

    public User(String username, String email, String passwordHash, UserRole role) {
        super();
        this.username = username;
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    @Override
    public String label() {
        return username;
    }

    /** Linha usada nas listagens administrativas. */
    public String describe() {
        return username + " <" + email + "> - " + role.label();
    }
}
