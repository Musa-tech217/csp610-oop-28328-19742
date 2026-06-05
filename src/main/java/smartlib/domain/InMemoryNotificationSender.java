package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.util.ArrayList;
import java.util.List;

/**
 * Test-friendly notification sender that records sent notifications.
 */
public final class InMemoryNotificationSender implements NotificationSender {

    private final List<Notification> sentNotifications = new ArrayList<>();

    @Override
    public void send(Notification notification) {
        sentNotifications.add(notification);
    }

    public List<Notification> sentNotifications() {
        return List.copyOf(sentNotifications);
    }
}
