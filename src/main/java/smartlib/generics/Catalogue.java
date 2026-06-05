package smartlib.generics;
// Quick note: I tried to keep the generic types clear rather than overly clever.

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import smartlib.domain.Borrowable;

/**
 * Type-safe catalogue for borrowable, naturally comparable items.
 *
 * @param <T> item type that is borrowable and naturally sortable
 */
public final class Catalogue<T extends Borrowable & Comparable<T>> {

    private final List<T> items = new ArrayList<>();

    public void add(T item) {
        items.add(Objects.requireNonNull(item, "item must not be null"));
    }

    public void remove(T item) {
        items.remove(Objects.requireNonNull(item, "item must not be null"));
    }

    public List<T> search(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return items.stream()
                .filter(predicate)
                .toList();
    }

    public <K extends Comparable<? super K>> List<T> sortedBy(Function<? super T, ? extends K> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor must not be null");
        return items.stream()
                .sorted(Comparator.comparing(keyExtractor))
                .toList();
    }

    public void addAllAvailable(Catalogue<? extends T> source) {
        Objects.requireNonNull(source, "source must not be null");
        source.items.stream()
                .filter(Borrowable::isAvailableForBorrow)
                .forEach(this::add);
    }

    public void updateMatching(Predicate<T> predicate, Consumer<? super T> updater) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        Objects.requireNonNull(updater, "updater must not be null");
        items.stream()
                .filter(predicate)
                .forEach(updater);
    }

    public <K> Map<K, Long> groupCount(Function<? super T, ? extends K> classifier) {
        Objects.requireNonNull(classifier, "classifier must not be null");
        Map<K, Long> grouped = items.stream()
                .collect(Collectors.groupingBy(classifier, LinkedHashMap::new, Collectors.counting()));
        return Map.copyOf(grouped);
    }

    public List<T> items() {
        return List.copyOf(items);
    }
}
