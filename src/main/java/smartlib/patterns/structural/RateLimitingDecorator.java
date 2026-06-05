package smartlib.patterns.structural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import smartlib.domain.Notification;

/**
 * Limits the number of notifications each recipient can receive through this service instance.
 */
public final class RateLimitingDecorator extends NotificationDecorator {

    private final int maxNotificationsPerRecipient;
    private final ConcurrentHashMap<String, AtomicInteger> deliveriesByRecipient = new ConcurrentHashMap<>();

    public RateLimitingDecorator(NotificationService delegate, int maxNotificationsPerRecipient) {
        super(delegate);
        if (maxNotificationsPerRecipient < 1) {
            throw new IllegalArgumentException("maxNotificationsPerRecipient must be at least 1");
        }
        this.maxNotificationsPerRecipient = maxNotificationsPerRecipient;
    }

    @Override
    public void send(Notification notification) {
        Objects.requireNonNull(notification, "notification must not be null");
        AtomicInteger deliveryCount = deliveriesByRecipient.computeIfAbsent(
                notification.recipient(),
                ignored -> new AtomicInteger());
        if (deliveryCount.incrementAndGet() > maxNotificationsPerRecipient) {
            deliveryCount.decrementAndGet();
            throw new IllegalStateException("Rate limit exceeded for recipient " + notification.recipient());
        }
        super.send(notification);
    }
}
