package javaminnder.repository;

import java.util.List;
import java.util.Optional;
import javaminnder.model.BaseEntity;

/**
 * Contrato de persistência do JavaMinder.
 *
 * <p>Toda implementação (hoje JSON em arquivo) precisa oferecer estas operações,
 * o que permite trocar o armazenamento sem mexer nos serviços.</p>
 *
 * @param <T> tipo da entidade persistida
 */
public interface Repository<T extends BaseEntity> {

    T save(T entity);

    T update(T entity);

    Optional<T> findById(long id);

    List<T> findAll();

    boolean deleteById(long id);

    int count();
}
