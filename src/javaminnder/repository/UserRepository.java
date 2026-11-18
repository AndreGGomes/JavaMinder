package javaminnder.repository;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javaminnder.model.User;
import javaminnder.model.UserRole;
import javaminnder.storage.Json;

/** Persiste usuários em {@code users.json}. */
public class UserRepository extends AbstractJsonRepository<User> {

    public UserRepository(Path file) {
        super(file);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String normalized = email.trim().toLowerCase();
        return findAll().stream()
                .filter(user -> user.getEmail() != null && user.getEmail().equals(normalized))
                .findFirst();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    protected String entityName() {
        return "Usuário";
    }

    @Override
    protected User fromJson(Map<String, Object> data) {
        UserRole role = UserRole.valueOf(Json.getString(data, "role"));
        User user = new User(
                Json.getString(data, "username"),
                Json.getString(data, "email"),
                Json.getString(data, "passwordHash"),
                role);
        user.setId(Json.getLong(data, "id", 0L));
        user.setCreatedAt(dateTimeValue(data, "createdAt"));
        user.setUpdatedAt(dateTimeValue(data, "updatedAt"));
        return user;
    }

    @Override
    protected Map<String, Object> toJson(User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("passwordHash", user.getPasswordHash());
        data.put("role", user.getRole().name());
        data.put("createdAt", String.valueOf(user.getCreatedAt()));
        data.put("updatedAt", String.valueOf(user.getUpdatedAt()));
        return data;
    }
}
