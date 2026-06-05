package smartlib.concurrent;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.BookRepository;
import smartlib.domain.BorrowingPolicyRegistry;
import smartlib.domain.DefaultFineCalculator;
import smartlib.domain.Fine;
import smartlib.domain.FineRepository;
import smartlib.domain.Loan;
import smartlib.domain.LoanRepository;
import smartlib.domain.Member;
import smartlib.domain.MemberRepository;
import smartlib.domain.MembershipType;
import smartlib.domain.Notification;
import smartlib.domain.NotificationSender;
import smartlib.domain.NotificationChannel;
import smartlib.domain.PremiumPolicy;
import smartlib.domain.StandardPolicy;
import smartlib.domain.StudentPolicy;
import smartlib.patterns.behavioural.EventType;
import smartlib.patterns.behavioural.LibraryEvent;
import smartlib.patterns.behavioural.LibraryEventBus;

class BookReturnProcessorTest {

    private BookRepository bookRepository;
    private MemberRepository memberRepository;
    private LoanRepository loanRepository;
    private FineRepository fineRepository;
    private RecordingNotificationSender notificationSender;
    private LibraryEventBus eventBus;
    private BookReturnProcessor processor;
    private Clock clock;
    private List<LibraryEvent> publishedEvents;

    @BeforeEach
    void setUp() {
        bookRepository = new BookRepository();
        memberRepository = new MemberRepository();
        loanRepository = new LoanRepository();
        fineRepository = new FineRepository();
        notificationSender = new RecordingNotificationSender();
        publishedEvents = new CopyOnWriteArrayList<>();
        eventBus = new LibraryEventBus();
        eventBus.subscribe(publishedEvents::add);
        clock = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);

        BorrowingPolicyRegistry policies = new BorrowingPolicyRegistry(List.of(
                new StandardPolicy(),
                new PremiumPolicy(),
                new StudentPolicy()));

        processor = new BookReturnProcessor(
                loanRepository,
                bookRepository,
                memberRepository,
                fineRepository,
                notificationSender,
                eventBus,
                new DefaultFineCalculator(policies),
                clock,
                4);
    }

    @AfterEach
    void tearDown() {
        processor.shutdown();
    }

    @Test
    void processBatchReturnsAllResultsAndPublishesEvents() {
        seedReturnableLoan("loan-1", "book-1", "member-1", MembershipType.STANDARD, LocalDate.of(2026, 5, 1), 14);
        seedReturnableLoan("loan-2", "book-2", "member-2", MembershipType.PREMIUM, LocalDate.of(2026, 5, 10), 28);

        List<ReturnResult> results = processor.processBatch(List.of("loan-1", "loan-2")).join();

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(ReturnResult::success));
        assertEquals(2, processor.processedCount());
        assertEquals(0, processor.failedCount());
        assertEquals(2, publishedEvents.stream().filter(event -> event.type() == EventType.BOOK_RETURNED).count());
        assertEquals(1, bookRepository.findById("book-1").orElseThrow().availableCopies());
        assertEquals(1, bookRepository.findById("book-2").orElseThrow().availableCopies());
    }

    @Test
    void failedReturnDoesNotCrashPoolAndIncrementsFailedCounter() {
        ReturnResult result = processor.processReturn("missing-loan").join();

        assertFalse(result.success());
        assertEquals(0, processor.processedCount());
        assertEquals(1, processor.failedCount());
        assertTrue(result.message().contains("Loan not found"));
    }

    @Test
    void processorTracksFineCalculationAndNotificationSending() {
        seedReturnableLoan("loan-1", "book-1", "member-1", MembershipType.STANDARD, LocalDate.of(2026, 5, 1), 14);

        ReturnResult result = processor.processReturn("loan-1").join();

        assertTrue(result.success());
        assertEquals(new BigDecimal("15.50"), result.fineAmount());
        assertEquals(1, fineRepository.findAll().size());
        assertEquals(1, notificationSender.sentNotifications().size());
    }

    @Test
    void multipleConcurrentReturnsKeepCountersCorrect() {
        seedReturnableLoan("loan-1", "book-1", "member-1", MembershipType.STANDARD, LocalDate.of(2026, 5, 1), 14);
        seedReturnableLoan("loan-2", "book-2", "member-2", MembershipType.STUDENT, LocalDate.of(2026, 5, 20), 21);
        seedReturnableLoan("loan-3", "book-3", "member-3", MembershipType.PREMIUM, LocalDate.of(2026, 5, 25), 28);

        List<CompletableFuture<ReturnResult>> futures = List.of(
                processor.processReturn("loan-1"),
                processor.processReturn("loan-2"),
                processor.processReturn("loan-3"));

        List<ReturnResult> results = futures.stream().map(CompletableFuture::join).toList();

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(ReturnResult::success));
        assertEquals(3, processor.processedCount());
        assertEquals(0, processor.failedCount());
    }

    private void seedReturnableLoan(
            String loanId,
            String bookId,
            String memberId,
            MembershipType membershipType,
            LocalDate borrowedOn,
            int loanPeriodDays) {
        memberRepository.save(new Member(
                memberId,
                "Member " + memberId,
                memberId + "@example.com",
                membershipType,
                BigDecimal.ZERO,
                NotificationChannel.EMAIL));
        bookRepository.save(new Book(bookId, "Book " + bookId, "Author " + bookId, "ISBN-" + bookId, "Programming", 2020, 0));
        loanRepository.save(Loan.open(loanId, bookId, memberId, borrowedOn, loanPeriodDays));
    }

    private static final class RecordingNotificationSender implements NotificationSender {

        private final List<Notification> sentNotifications = new CopyOnWriteArrayList<>();

        @Override
        public void send(Notification notification) {
            sentNotifications.add(notification);
        }

        List<Notification> sentNotifications() {
            return List.copyOf(sentNotifications);
        }
    }
}
