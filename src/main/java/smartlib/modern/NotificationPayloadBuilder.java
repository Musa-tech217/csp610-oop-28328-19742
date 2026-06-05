package smartlib.modern;
// Quick note: I used the newer Java feature here because it made the intent clearer.

import java.time.LocalDateTime;
import java.util.Objects;
import smartlib.domain.Member;
import smartlib.domain.NotificationChannel;

/**
 * Builds JSON notification payloads using text blocks.
 */
public final class NotificationPayloadBuilder {

    public String build(Member member, NotificationChannel channel, String message, LocalDateTime timestamp) {
        Objects.requireNonNull(member, "member must not be null");
        Objects.requireNonNull(channel, "channel must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");

        return """
                {
                  "recipientId": "%s",
                  "recipientEmail": "%s",
                  "channel": "%s",
                  "message": "%s",
                  "timestamp": "%s"
                }
                """.formatted(
                escape(member.id()),
                escape(member.notificationAddress()),
                escape(channel.name()),
                escape(message),
                escape(timestamp.toString()));
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
