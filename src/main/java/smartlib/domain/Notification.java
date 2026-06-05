package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable notification message to be persisted and sent.
 */
public record Notification(
        String id,
        String memberId,
        String recipient,
        NotificationChannel channel,
        String subject,
        String message,
        LocalDateTime createdAt) {

    public Notification {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(channel, "channel must not be null");
        Objects.requireNonNull(subject, "subject must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
