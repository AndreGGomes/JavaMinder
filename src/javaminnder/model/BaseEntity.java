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

    private /*@ spec_public nullable @*/ Long id;
    private /*@ spec_public nullable @*/ LocalDateTime createdAt;
    private /*@ spec_public nullable @*/ LocalDateTime updatedAt;

    /*@ protected behavior
      @*/
    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /*@ public normal_behavior
      @   ensures \result == id;
      @*/
    public /*@ pure nullable @*/ Long getId() {
        return id;
    }

    /*@ public normal_behavior
      @   ensures id == newId;
      @*/
    public void setId(/*@ nullable @*/ Long newId) {
        this.id = newId;
    }

    /*@ public normal_behavior
      @   ensures \result == createdAt;
      @*/
    public /*@ pure nullable @*/ LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /*@ public normal_behavior
      @   ensures createdAt == newCreatedAt;
      @*/
    public void setCreatedAt(/*@ nullable @*/ LocalDateTime newCreatedAt) {
        this.createdAt = newCreatedAt;
    }

    /*@ public normal_behavior
      @   ensures \result == updatedAt;
      @*/
    public /*@ pure nullable @*/ LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /*@ public normal_behavior
      @   ensures updatedAt == newUpdatedAt;
      @*/
    public void setUpdatedAt(/*@ nullable @*/ LocalDateTime newUpdatedAt) {
        this.updatedAt = newUpdatedAt;
    }

    /** Marca a entidade como modificada agora. */
    /*@ public behavior
      @*/
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /*@ also
      @ public normal_behavior
      @   ensures true;
      @*/
    @Override
    public /*@ pure @*/ boolean equals(/*@ nullable @*/ Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BaseEntity entity = (BaseEntity) other;
        return id != null && id.equals(entity.id);
    }

    /*@ also
    @ public behavior
    @*/
    @Override
    public /*@ pure skipesc @*/ int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
