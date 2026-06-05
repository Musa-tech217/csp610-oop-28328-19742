package smartlib.modern;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.Fine;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.Notification;
import smartlib.domain.NotificationChannel;
import smartlib.domain.Reservation;

class EntityDescriberTest {

    private final EntityDescriber describer = new EntityDescriber();

    @Test
    void describesSupportedEntitiesWithPatternMatching() {
        assertTrue(describer.describe(new Book("book-1", "Clean Code", "Robert Martin", "ISBN-1", "Programming", 2008, 1))
                .contains("Book["));
        assertTrue(describer.describe(new Member("member-1", "Alice", "alice@example.com", MembershipType.STANDARD,
                BigDecimal.ZERO, NotificationChannel.EMAIL)).contains("Member["));
        assertTrue(describer.describe(Loan.open("loan-1", "book-1", "member-1", LocalDate.of(2026, 6, 1), 14))
                .contains("Loan["));
        assertTrue(describer.describe(Reservation.create("reservation-1", "book-1", "member-1", LocalDate.of(2026, 6, 20)))
                .contains("Reservation["));
        assertTrue(describer.describe(new Fine("fine-1", "loan-1", "member-1", new BigDecimal("1.00"),
                LocalDate.of(2026, 6, 20), false)).contains("Fine["));
        assertTrue(describer.describe(new Notification("notification-1", "member-1", "alice@example.com",
                NotificationChannel.EMAIL, "Subject", "Message", LocalDateTime.of(2026, 6, 20, 10, 0)))
                .contains("Notification["));
    }
}
