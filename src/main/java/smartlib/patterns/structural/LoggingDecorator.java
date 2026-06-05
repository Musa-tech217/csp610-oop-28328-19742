package smartlib.patterns.structural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;
import smartlib.domain.Notification;

/**
 * Adds audit-friendly logging without changing the wrapped notification service.
 */
public final class LoggingDecorator extends NotificationDecorator {

    private final Consumer<String> logger;
    private final Clock clock;

    public LoggingDecorator(NotificationService delegate) {
        this(delegate, System.out::println, Clock.systemUTC());
    }

    public LoggingDecorator(NotificationService delegate, Consumer<String> logger, Clock clock) {
        super(delegate);
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void send(Notification notification) {
        Instant now = clock.instant();
        logger.accept(now + " recipient=" + notification.recipient() + " message=" + notification.message());
        super.send(notification);
    }
}
