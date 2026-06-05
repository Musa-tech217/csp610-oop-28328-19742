package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

/**
 * Sends notifications through an external mechanism.
 */
public interface NotificationSender {

    void send(Notification notification);
}
