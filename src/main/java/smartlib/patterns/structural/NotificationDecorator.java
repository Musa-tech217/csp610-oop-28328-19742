package smartlib.patterns.structural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.util.Objects;
import smartlib.domain.Notification;

/**
 * Shared base class for decorators that enrich notification sending.
 */
public abstract class NotificationDecorator implements NotificationService {

    private final NotificationService delegate;

    protected NotificationDecorator(NotificationService delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    protected NotificationService delegate() {
        return delegate;
    }

    @Override
    public void send(Notification notification) {
        delegate.send(notification);
    }
}
