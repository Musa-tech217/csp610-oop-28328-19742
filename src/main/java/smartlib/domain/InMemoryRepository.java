package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Thread-safe in-memory repository for learning and testing.
 *
 * @param <T> entity type
 * @param <ID> identifier type
 */
public class InMemoryRepository<T, ID> implements Repository<T, ID> {

    private final ConcurrentHashMap<ID, T> storage = new ConcurrentHashMap<>();
    private final Function<T, ID> idExtractor;

    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = Objects.requireNonNull(idExtractor, "idExtractor must not be null");
    }

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        storage.put(idExtractor.apply(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public void deleteById(ID id) {
        Objects.requireNonNull(id, "id must not be null");
        storage.remove(id);
    }
}
