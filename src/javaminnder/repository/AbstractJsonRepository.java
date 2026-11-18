package javaminnder.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javaminnder.exception.NotFoundException;
import javaminnder.model.BaseEntity;
import javaminnder.storage.Json;

/**
 * Repositório genérico com persistência em um único arquivo JSON.
 *
 * <p>A classe concentra o que é comum a qualquer entidade: controle de ids,
 * carregamento sob demanda, gravação atômica e busca por id. As subclasses
 * apenas traduzem a entidade para/de {@code Map<String, Object>}.</p>
 *
 * @param <T> tipo da entidade
 */
public abstract class AbstractJsonRepository<T extends BaseEntity> implements Repository<T> {

    private final Path file;
    private final List<T> items = new ArrayList<>();
    private boolean loaded;

    protected AbstractJsonRepository(Path file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    protected abstract T fromJson(Map<String, Object> data);

    protected abstract Map<String, Object> toJson(T entity);

    /** Nome singular usado nas mensagens de erro. */
    protected abstract String entityName();

    public Path storageFile() {
        return file;
    }

    @Override
    public synchronized List<T> findAll() {
        ensureLoaded();
        return List.copyOf(items);
    }

    @Override
    public synchronized Optional<T> findById(long id) {
        ensureLoaded();
        return items.stream()
                .filter(item -> item.getId() != null && item.getId() == id)
                .findFirst();
    }

    @Override
    public synchronized T save(T entity) {
        ensureLoaded();
        if (entity.getId() == null) {
            entity.setId(nextId());
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        items.add(entity);
        flush();
        return entity;
    }

    @Override
    public synchronized T update(T entity) {
        ensureLoaded();
        for (int index = 0; index < items.size(); index++) {
            T current = items.get(index);
            if (Objects.equals(current.getId(), entity.getId())) {
                entity.setCreatedAt(current.getCreatedAt());
                entity.setUpdatedAt(LocalDateTime.now());
                items.set(index, entity);
                flush();
                return entity;
            }
        }
        throw new NotFoundException(entityName() + " não encontrado: id " + entity.getId());
    }

    @Override
    public synchronized boolean deleteById(long id) {
        ensureLoaded();
        boolean removed = items.removeIf(item -> item.getId() != null && item.getId() == id);
        if (removed) {
            flush();
        }
        return removed;
    }

    @Override
    public synchronized int count() {
        ensureLoaded();
        return items.size();
    }

    protected synchronized long nextId() {
        ensureLoaded();
        return items.stream()
                .filter(item -> item.getId() != null)
                .map(BaseEntity::getId)
                .max(Comparator.naturalOrder())
                .orElse(0L)
                + 1L;
    }

    protected static LocalDateTime dateTimeValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : LocalDateTime.parse(String.valueOf(value));
    }

    protected static LocalDate dateValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : LocalDate.parse(String.valueOf(value));
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        if (!Files.exists(file)) {
            return;
        }
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            if (content.isBlank()) {
                return;
            }
            Object parsed = Json.parse(content);
            if (!(parsed instanceof List<?> list)) {
                throw new IllegalStateException(
                        "O arquivo " + file + " deveria conter uma lista JSON.");
            }
            for (Object entry : list) {
                items.add(fromJson(Json.asObject(entry)));
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao ler " + file, exception);
        }
    }

    /** Grava todo o conteúdo em disco de forma atômica (arquivo temporário + move). */
    protected synchronized void flush() {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            List<Object> payload = new ArrayList<>(items.size());
            for (T item : items) {
                payload.add(toJson(item));
            }
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, Json.stringify(payload), StandardCharsets.UTF_8);
            Files.move(
                    temporary,
                    file,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao gravar " + file, exception);
        }
    }
}
