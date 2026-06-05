package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.util.List;
import java.util.Optional;

/**
 * Generic repository abstraction for persistence operations.
 *
 * @param <T> stored entity type
 * @param <ID> entity identifier type
 */
public interface Repository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    void deleteById(ID id);
}
