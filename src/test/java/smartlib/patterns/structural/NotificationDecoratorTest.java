package smartlib.patterns.structural;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import smartlib.domain.Notification;
import smartlib.domain.NotificationChannel;

class NotificationDecoratorTest {

    @Test
    void loggingDecoratorLogsAndDelegates() {
        List<String> logEntries = new ArrayList<>();
        AtomicInteger deliveries = new AtomicInteger();
        NotificationService service = new LoggingDecorator(
                new EmailNotificationService(notification -> deliveries.incrementAndGet()),
                logEntries::add,
                Clock.fixed(Instant.parse("2026-05-28T12:00:00Z"), ZoneOffset.UTC));

        service.send(notification("member@example.com", "Hello"));

        assertEquals(1, deliveries.get());
        assertEquals(1, logEntries.size());
        assertEquals("2026-05-28T12:00:00Z recipient=member@example.com message=Hello", logEntries.get(0));
    }

    @Test
    void retryDecoratorRetriesFailedSends() {
        AtomicInteger attempts = new AtomicInteger();
        List<Long> sleepDurations = new ArrayList<>();
        NotificationService service = new RetryDecorator(
                new EmailNotificationService(notification -> {
                    if (attempts.incrementAndGet() < 3) {
                        throw new IllegalStateException("Temporary failure");
                    }
                }),
                3,
                java.time.Duration.ofMillis(100),
                sleepDurations::add);

        service.send(notification("member@example.com", "Retry"));

        assertEquals(3, attempts.get());
        assertEquals(List.of(100L, 200L), sleepDurations);
    }

    @Test
    void rateLimitingDecoratorBlocksAfterLimitPerRecipient() {
        AtomicInteger deliveries = new AtomicInteger();
        NotificationService service = new RateLimitingDecorator(
                new EmailNotificationService(notification -> deliveries.incrementAndGet()),
                2);

        service.send(notification("member@example.com", "One"));
        service.send(notification("member@example.com", "Two"));

        assertThrows(IllegalStateException.class,
                () -> service.send(notification("member@example.com", "Three")));
        assertEquals(2, deliveries.get());
    }

    @Test
    void decoratorsComposeInOrder() {
        List<String> trace = new ArrayList<>();
        AtomicInteger attempts = new AtomicInteger();

        NotificationService service = new RateLimitingDecorator(
                new RetryDecorator(
                        new LoggingDecorator(
                                new EmailNotificationService(notification -> {
                                    trace.add("gateway-" + attempts.incrementAndGet());
                                    if (attempts.get() == 1) {
                                        throw new IllegalStateException("First attempt fails");
                                    }
                                }),
                                message -> trace.add("log"),
                                Clock.fixed(Instant.parse("2026-05-28T12:00:00Z"), ZoneOffset.UTC)),
                        2,
                        java.time.Duration.ofMillis(100),
                        delay -> trace.add("sleep-" + delay)),
                1);

        service.send(notification("member@example.com", "Composed"));
        assertEquals(List.of("log", "gateway-1", "sleep-100", "log", "gateway-2"), trace);

        assertThrows(IllegalStateException.class,
                () -> service.send(notification("member@example.com", "Blocked outside")));
        assertEquals(List.of("log", "gateway-1", "sleep-100", "log", "gateway-2"), trace);
    }

    private Notification notification(String recipient, String message) {
        return new Notification(
                "notification-1",
                "member-1",
                recipient,
                NotificationChannel.EMAIL,
                "Subject",
                message,
                LocalDateTime.of(2026, 5, 28, 12, 0));
    }
}
