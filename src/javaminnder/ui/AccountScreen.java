package javaminnder.ui;

import java.time.format.DateTimeFormatter;
import javaminnder.exception.AppException;
import javaminnder.model.User;
import javaminnder.service.AuthService;
import javaminnder.service.Session;

/** Dados da conta e troca de senha. */
public class AccountScreen {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Console console;
    private final Session session;
    private final AuthService authService;

    public AccountScreen(Console console, Session session, AuthService authService) {
        this.console = console;
        this.session = session;
        this.authService = authService;
    }

    public void show() {
        boolean running = true;
        while (running) {
            User user = session.currentUser();
            console.println(Menu.header("Minha conta"));
            console.println(Menu.message("Nome  : " + user.getUsername()));
            console.println(Menu.message("E-mail: " + user.getEmail()));
            console.println(Menu.message("Perfil: " + user.getRole().label()));
            console.println(Menu.message("Criada em " + user.getCreatedAt().format(TIMESTAMP)));
            console.println();
            console.println(Menu.option(1, "Alterar senha"));
            console.println(Menu.option(0, "Voltar ao painel"));
            console.println();
            int choice = console.readInt("  Opção: ", 0, 1);
            switch (choice) {
                case 1 -> changePassword(user);
                case 0 -> running = false;
                default -> {
                    // opção já validada
                }
            }
        }
    }

    private void changePassword(User user) {
        String current = console.readLine("  Senha atual: ");
        String newPassword = console.readLine("  Nova senha (mínimo 8 caracteres): ");
        String confirmation = console.readLine("  Confirme a nova senha: ");
        if (!newPassword.equals(confirmation)) {
            console.println(Menu.error("As senhas não conferem."));
            return;
        }
        try {
            authService.changePassword(user, current, newPassword);
            console.println(Menu.message("Senha alterada com sucesso."));
        } catch (AppException exception) {
            console.println(Menu.error(exception.getMessage()));
        }
    }
}
