package smartlib.modern;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.InMemoryRepository;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.Notification;
import smartlib.domain.NotificationChannel;
import smartlib.domain.NotificationSender;
import smartlib.domain.Reservation;
import smartlib.patterns.behavioural.EventType;
import smartlib.patterns.behavioural.LibraryEvent;
import smartlib.patterns.behavioural.LibraryEventBus;

class LoanResultHandlerTest {

    private InMemoryRepository<Reservation, String> reservationRepository;
    private RecordingNotificationSender notificationSender;
    private List<String> logs;
    private List<LibraryEvent> events;
    private LoanResultHandler handler;
    private Member requester;

    @BeforeEach
    void setUp() {
        reservationRepository = new InMemoryRepository<>(Reservation::id);
        notificationSender = new RecordingNotificationSender();
        logs = new ArrayList<>();
        events = new ArrayList<>();
        LibraryEventBus eventBus = new LibraryEventBus();
        eventBus.subscribe(events::add);
        handler = new LoanResultHandler(
                reservationRepository,
                notificationSender,
                eventBus,
                Clock.fixed(Instant.parse("2026-06-20T10:15:30Z"), ZoneOffset.UTC),
                logs::add);
        requester = new Member(
                "member-1",
                "Alice",
                "alice@example.com",
                MembershipType.STANDARD,
                BigDecimal.ZERO,
                NotificationChannel.EMAIL);
    }

    @Test
    void successPublishesBookBorrowedEvent() {
        Loan loan = Loan.open("loan-1", "book-1", "member-1", LocalDate.of(2026, 6, 20), 14);

        String result = handler.handle(new LoanResult.Success(loan), requester);

        assertEquals("Borrow succeeded", result);
        assertTrue(logs.get(0).contains("Loan created successfully"));
        assertEquals(EventType.BOOK_BORROWED, events.get(0).type());
    }

    @Test
    void insufficientCopiesCreatesReservationAndNotifiesMember() {
        Book book = new Book("book-1", "Clean Code", "Robert Martin", "ISBN-1", "Programming", 2008, 0);

        String result = handler.handle(new LoanResult.InsufficientCopies(book, 1, 0), requester);

        assertEquals("Reservation created", result);
        assertEquals(1, reservationRepository.findAll().size());
        assertEquals(1, notificationSender.notifications.size());
        assertTrue(notificationSender.notifications.get(0).message().contains("reservation"));
    }

    @Test
    void memberSuspendedLogsWarning() {
        String result = handler.handle(new LoanResult.MemberSuspended(requester, "Unpaid account review"), requester);

        assertEquals("Member suspended", result);
        assertTrue(logs.get(0).contains("WARNING"));
    }

    @Test
    void fineExceededSendsPaymentReminder() {
        String result = handler.handle(
                new LoanResult.FineExceeded(requester, new BigDecimal("15.00"), new BigDecimal("10.00")),
                requester);

        assertEquals("Payment reminder sent", result);
        assertEquals(1, notificationSender.notifications.size());
        assertTrue(notificationSender.notifications.get(0).message().contains("Outstanding fine"));
    }

    private static final class RecordingNotificationSender implements NotificationSender {

        private final List<Notification> notifications = new ArrayList<>();

        @Override
        public void send(Notification notification) {
            notifications.add(notification);
        }
    }
}
