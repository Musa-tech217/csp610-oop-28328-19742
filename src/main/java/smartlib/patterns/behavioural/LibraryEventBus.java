package smartlib.patterns.behavioural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Synchronous, listener-friendly event bus that protects publication from listener failures.
 */
public final class LibraryEventBus {

    private final CopyOnWriteArrayList<LibraryEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Consumer<RuntimeException> errorHandler;

    public LibraryEventBus() {
        this(ignored -> {
        });
    }

    public LibraryEventBus(Consumer<RuntimeException> errorHandler) {
        this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler must not be null");
    }

    public void subscribe(LibraryEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener must not be null"));
    }

    public void unsubscribe(LibraryEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(LibraryEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        for (LibraryEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException exception) {
                errorHandler.accept(exception);
            }
        }
    }

    public List<LibraryEventListener> listeners() {
        return List.copyOf(listeners);
    }
}
