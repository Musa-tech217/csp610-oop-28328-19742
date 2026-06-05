package smartlib.patterns.behavioural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable domain event carrying a typed event name and payload map.
 */
public record LibraryEvent(EventType type, LocalDateTime occurredAt, Map<String, Object> payload) {

    public LibraryEvent {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        payload = Map.copyOf(Objects.requireNonNull(payload, "payload must not be null"));
    }

    public static LibraryEvent of(EventType type, Map<String, Object> payload) {
        return new LibraryEvent(type, LocalDateTime.now(), payload);
    }

    public <T> T payload(String key, Class<T> expectedType) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(expectedType, "expectedType must not be null");
        Object value = payload.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing payload value for key " + key);
        }
        if (!expectedType.isInstance(value)) {
            throw new IllegalArgumentException("Payload value for key " + key + " is not of type " + expectedType.getSimpleName());
        }
        return expectedType.cast(value);
    }
}
