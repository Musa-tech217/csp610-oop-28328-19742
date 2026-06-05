package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

public final class NotificationRepository extends InMemoryRepository<Notification, String> {

    public NotificationRepository() {
        super(Notification::id);
    }
}
