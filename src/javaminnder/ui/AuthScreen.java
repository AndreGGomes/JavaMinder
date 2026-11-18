package javaminnder.ui;

import javaminnder.exception.AppException;
import javaminnder.model.User;
import javaminnder.service.AuthService;

/** Tela de acesso: login e criação de conta. */
public class AuthScreen {

    private final Console console;
    private final AuthService authService;

    public AuthScreen(Console console, AuthService authService) {
        this.console = console;
        this.authService = authService;
    }

    public void show() {
        while (true) {
            console.println(Menu.header("JavaMinder - Acesso"));
            console.println(Menu.option(1, "Entrar"));
            console.println(Menu.option(2, "Criar conta"));
            console.println(Menu.option(0, "Encerrar programa"));
            console.println();
            int choice = console.readInt("  Opção: ", 0, 2);
            try {
                switch (choice) {
                    case 1 -> login();
                    case 2 -> createAccount();
                    case 0 -> throw new AppExitException("Sessão encerrada.");
                    default -> {
                        // opção já validada
                    }
                }
            } catch (AppException exception) {
                console.println(Menu.error(exception.getMessage()));
            }
            if (authService.isAuthenticated()) {
                return;
            }
        }
    }

    private void login() {
        String email = console.readLine("  E-mail: ");
        String password = console.readLine("  Senha: ");
        User user = authService.login(email, password);
        console.println(Menu.message("Login efetuado. Bem-vindo(a), " + user.getUsername() + "!"));
    }

    private void createAccount() {
        console.println(Menu.message("Nova conta"));
        String username = console.readLine("  Nome: ");
        String email = console.readLine("  E-mail: ");
        String password = console.readLine("  Senha (mínimo 8 caracteres): ");
        String confirmation = console.readLine("  Confirme a senha: ");
        if (!password.equals(confirmation)) {
            console.println(Menu.error("As senhas não conferem."));
            return;
        }
        User created = authService.register(username, email, password);
        authService.login(created.getEmail(), password);
        console.println(Menu.message("Conta criada! Perfil: " + created.getRole().label() + "."));
    }
}
