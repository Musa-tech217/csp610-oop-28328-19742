package smartlib.patterns.structural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import smartlib.domain.Notification;

/**
 * Core notification component for the Decorator pattern example.
 */
public interface NotificationService {

    void send(Notification notification);
}
