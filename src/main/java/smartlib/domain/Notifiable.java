package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

/**
 * Exposes the information needed to notify a domain entity.
 */
public interface Notifiable {

    String notificationAddress();

    NotificationChannel preferredChannel();
}
