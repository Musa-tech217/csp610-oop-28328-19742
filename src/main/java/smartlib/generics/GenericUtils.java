package smartlib.generics;
// Quick note: I tried to keep the generic types clear rather than overly clever.

import java.util.Objects;
import java.util.Optional;

/**
 * Utility methods for runtime-safe generic operations.
 */
public final class GenericUtils {

    private GenericUtils() {
    }

    public static <T> Optional<T> safeCast(Object obj, Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        if (clazz.isInstance(obj)) {
            return Optional.of(clazz.cast(obj));
        }
        return Optional.empty();
    }
}
