package smartlib.patterns.behavioural;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import smartlib.domain.DefaultFineCalculator;
import smartlib.domain.Fine;
import smartlib.domain.FineRepository;
import smartlib.domain.InMemoryRepository;
import smartlib.domain.Member;
import smartlib.domain.MemberRepository;
import smartlib.domain.MembershipType;
import smartlib.domain.Notification;
import smartlib.domain.NotificationChannel;
import smartlib.domain.NotificationSender;
import smartlib.domain.NotificationRepository;
import smartlib.domain.PremiumPolicy;
import smartlib.domain.StandardPolicy;
import smartlib.domain.StudentPolicy;
import smartlib.domain.BorrowingPolicyRegistry;
import smartlib.domain.Loan;

class LibraryEventBusTest {

    @Test
    void listenerReceivesCorrectPayload() {
        LibraryEventBus eventBus = new LibraryEventBus();
        AtomicReference<LibraryEvent> received = new AtomicReference<>();

        eventBus.subscribe(received::set);
        LibraryEvent event = LibraryEvent.of(EventType.RESERVATION_EXPIRED, Map.of("reservationId", "reservation-1"));

        eventBus.publish(event);

        assertEquals(event, received.get());
        assertEquals("reservation-1", received.get().payload("reservationId", String.class));
    }

    @Test
    void unsubscribeStopsFurtherNotifications() {
        LibraryEventBus eventBus = new LibraryEventBus();
        AtomicInteger callCount = new AtomicInteger();
        LibraryEventListener listener = event -> callCount.incrementAndGet();

        eventBus.subscribe(listener);
        eventBus.unsubscribe(listener);
        eventBus.publish(LibraryEvent.of(EventType.FINE_IMPOSED, Map.of("id", "fine-1")));

        assertEquals(0, callCount.get());
    }

    @Test
    void oneFailingListenerDoesNotStopOthers() {
        List<RuntimeException> errors = new ArrayList<>();
        LibraryEventBus eventBus = new LibraryEventBus(errors::add);
        AtomicInteger successfulCalls = new AtomicInteger();

        eventBus.subscribe(event -> {
            throw new IllegalStateException("boom");
        });
        eventBus.subscribe(event -> successfulCalls.incrementAndGet());

        eventBus.publish(LibraryEvent.of(EventType.FINE_IMPOSED, Map.of("id", "fine-1")));

        assertEquals(1, successfulCalls.get());
        assertEquals(1, errors.size());
    }

    @Test
    void overdueFineListenerImposesFineAndPublishesFollowUpEvent() {
        BorrowingPolicyRegistry policies = new BorrowingPolicyRegistry(List.of(
                new StandardPolicy(),
                new PremiumPolicy(),
                new StudentPolicy()));
        MemberRepository memberRepository = new MemberRepository();
        FineRepository fineRepository = new FineRepository();
        List<LibraryEvent> followUpEvents = new ArrayList<>();
        LibraryEventBus eventBus = new LibraryEventBus();
        eventBus.subscribe(followUpEvents::add);

        Member member = member();
        memberRepository.save(member);
        OverdueFineListener listener = new OverdueFineListener(
                new DefaultFineCalculator(policies),
                fineRepository,
                memberRepository,
                eventBus);

        Loan loan = Loan.open("loan-1", "book-1", member.id(), LocalDate.of(2026, 5, 1), 14);
        listener.onEvent(LibraryEvent.of(
                EventType.BOOK_RETURNED,
                Map.of("loan", loan, "member", member, "returnedOn", LocalDate.of(2026, 5, 20))));

        Fine savedFine = fineRepository.findById("fine-loan-1").orElseThrow();
        Member updatedMember = memberRepository.findById(member.id()).orElseThrow();

        assertEquals(new BigDecimal("2.50"), savedFine.amount());
        assertEquals(new BigDecimal("2.50"), updatedMember.outstandingFine());
        assertTrue(followUpEvents.stream().anyMatch(event -> event.type() == EventType.FINE_IMPOSED));
    }

    @Test
    void memberNotificationListenerSendsNotificationsForRelevantEvents() {
        NotificationRepository notificationRepository = new NotificationRepository();
        NotificationSender notificationSender = mock(NotificationSender.class);
        MemberNotificationListener listener = new MemberNotificationListener(notificationRepository, notificationSender);
        Member member = member();

        listener.onEvent(LibraryEvent.of(
                EventType.FINE_IMPOSED,
                Map.of("member", member, "fine", new Fine("fine-1", "loan-1", member.id(), new BigDecimal("1.00"),
                        LocalDate.of(2026, 5, 28), false))));

        listener.onEvent(LibraryEvent.of(
                EventType.RESERVATION_EXPIRED,
                Map.of("member", member, "reservation",
                        smartlib.domain.Reservation.create("reservation-1", "book-1", member.id(), LocalDate.of(2026, 5, 20)))));

        assertEquals(2, notificationRepository.findAll().size());
        verify(notificationSender, times(2)).send(any(Notification.class));
    }

    private Member member() {
        return new Member(
                "member-1",
                "Test Member",
                "member@example.com",
                MembershipType.STANDARD,
                BigDecimal.ZERO,
                NotificationChannel.EMAIL);
    }
}
