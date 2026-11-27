package javaminnder.service;

import java.util.List;
import java.util.Optional;
import javaminnder.exception.AuthException;
import javaminnder.exception.ValidationException;
import javaminnder.model.User;
import javaminnder.model.UserRole;
import javaminnder.repository.UserRepository;
import javaminnder.security.Pbkdf2PasswordHasher;
import javaminnder.util.Validators;

/** Regras de cadastro, autenticação e troca de senha. */
public class AuthService {

    /*@ spec_public @*/ private final UserRepository users;
    /*@ spec_public @*/ private final Pbkdf2PasswordHasher passwordHasher;
    /*@ spec_public @*/ private final Session session;

    /*@ public normal_behavior
      @   requires users != null;
      @   requires passwordHasher != null;
      @   requires session != null;
      @   ensures this.users == users;
      @   ensures this.passwordHasher == passwordHasher;
      @   ensures this.session == session;
      @*/
    public AuthService(
            UserRepository users, Pbkdf2PasswordHasher passwordHasher, Session session) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.session = session;
    }

    /**
     * Cadastra um usuário. O primeiro usuário do sistema nasce como ADMIN,
     * os demais como USER.
     */
    /*@ public behavior
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    public User register(String username, String email, String password) {
        String cleanName = Validators.requireText(username, "nome", 2, 60);
        String cleanEmail = Validators.requireEmail(email);
        Validators.requirePassword(password);

        if (users.existsByEmail(cleanEmail)) {
            throw new ValidationException("Já existe uma conta com o e-mail " + cleanEmail + ".");
        }

        UserRole role = users.count() == 0 ? UserRole.ADMIN : UserRole.USER;
        User user = new User(cleanName, cleanEmail, passwordHasher.hash(password), role);
        users.save(user);
        return user;
    }

    /** Autentica e abre a sessão. Mensagem genérica para não revelar qual campo errou. */
    /*@ public behavior
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    public User login(String email, String password) {
        String cleanEmail = Validators.requireEmail(email);
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Informe a senha.");
        }
        User user = users.findByEmail(cleanEmail)
                .orElseThrow(() -> new AuthException("E-mail ou senha inválidos."));
        /*@ nullable @*/ String passwordHash = user.getPasswordHash();
        if (passwordHash == null || !passwordHasher.verify(password, passwordHash)) {
            throw new AuthException("E-mail ou senha inválidos.");
        }
        session.login(user);
        return user;
    }

    /*@ public normal_behavior
      @   assignable session.*;
      @*/
    public void logout() {
        session.logout();
    }

    /*@ public normal_behavior
      @   assignable \nothing;
      @*/
    public /*@ pure @*/ boolean isAuthenticated() {
        return session.isAuthenticated();
    }
    
    /*@ public behavior
      @   requires user != null;
      @   assignable \everything;
      @*/
    public void changePassword(User user, String currentPassword, String newPassword) {
        /*@ nullable @*/ String passwordHash = user.getPasswordHash();
        if (passwordHash == null || !passwordHasher.verify(currentPassword, passwordHash)) {
            throw new AuthException("Senha atual incorreta.");
        }
        Validators.requirePassword(newPassword);
        if (currentPassword.equals(newPassword)) {
            throw new ValidationException("A nova senha precisa ser diferente da atual.");
        }
        user.setPasswordHash(passwordHasher.hash(newPassword));
        user.touch();
        users.update(user);
    }

    /*@ public behavior
      @   requires users != null;
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    public List<User> listUsers() {
        return users.findAll();
    }

    /*@ public behavior
      @   requires users != null;
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    public Optional<User> findById(long id) {
        return users.findById(id);
    }
}
