package javaminnder.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Superclasse de todas as entidades persistidas.
 *
 * <p>Concentra identidade e auditoria (criação/atualização), de forma que
 * {@link User} e {@link Subscription} herdem esse comportamento em vez de repeti-lo.</p>
 */
public abstract class BaseEntity implements Describable {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Marca a entidade como modificada agora. */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BaseEntity entity = (BaseEntity) other;
        return id != null && id.equals(entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
