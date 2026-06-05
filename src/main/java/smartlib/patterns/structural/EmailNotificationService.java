package smartlib.patterns.structural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.util.Objects;
import smartlib.domain.Notification;

/**
 * Concrete base component that knows how to send email notifications.
 */
public final class EmailNotificationService implements NotificationService {

    private final NotificationGateway gateway;

    public EmailNotificationService() {
        this(notification -> {
        });
    }

    public EmailNotificationService(NotificationGateway gateway) {
        this.gateway = Objects.requireNonNull(gateway, "gateway must not be null");
    }

    @Override
    public void send(Notification notification) {
        gateway.deliver(notification);
    }
}
