package smartlib.modern;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.NotificationChannel;

class NotificationPayloadBuilderTest {

    @Test
    void buildsJsonPayloadUsingTextBlocks() {
        Member member = new Member(
                "member-1",
                "Alice",
                "alice@example.com",
                MembershipType.STANDARD,
                BigDecimal.ZERO,
                NotificationChannel.EMAIL);

        String payload = new NotificationPayloadBuilder().build(
                member,
                NotificationChannel.EMAIL,
                "Hello \"Alice\"",
                LocalDateTime.of(2026, 6, 20, 10, 15));

        assertTrue(payload.contains("\"recipientId\": \"member-1\""));
        assertTrue(payload.contains("\"message\": \"Hello \\\"Alice\\\"\""));
    }
}
