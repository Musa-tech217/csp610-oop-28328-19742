package smartlib.patterns.structural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.time.Duration;
import java.util.Objects;
import java.util.function.LongConsumer;
import smartlib.domain.Notification;

/**
 * Retries transient failures with exponential backoff.
 */
public final class RetryDecorator extends NotificationDecorator {

    private final int maxAttempts;
    private final Duration initialBackoff;
    private final LongConsumer sleeper;

    public RetryDecorator(NotificationService delegate, int maxAttempts) {
        this(delegate, maxAttempts, Duration.ofMillis(100), ignored -> {
        });
    }

    public RetryDecorator(NotificationService delegate, int maxAttempts, Duration initialBackoff, LongConsumer sleeper) {
        super(delegate);
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        this.maxAttempts = maxAttempts;
        this.initialBackoff = Objects.requireNonNull(initialBackoff, "initialBackoff must not be null");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper must not be null");
    }

    @Override
    public void send(Notification notification) {
        RuntimeException lastFailure = null;
        long delay = initialBackoff.toMillis();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                delegate().send(notification);
                return;
            } catch (RuntimeException exception) {
                lastFailure = exception;
                if (attempt == maxAttempts) {
                    throw exception;
                }
                sleeper.accept(delay);
                delay *= 2;
            }
        }

        throw lastFailure;
    }
}
