package smartlib.patterns.structural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import smartlib.domain.Notification;

@FunctionalInterface
public interface NotificationGateway {

    void deliver(Notification notification);
}
