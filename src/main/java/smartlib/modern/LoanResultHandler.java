package smartlib.modern;
// Quick note: I used the newer Java feature here because it made the intent clearer.

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.Notification;
import smartlib.domain.NotificationSender;
import smartlib.domain.Repository;
import smartlib.domain.Reservation;
import smartlib.patterns.behavioural.EventType;
import smartlib.patterns.behavioural.LibraryEvent;
import smartlib.patterns.behavioural.LibraryEventBus;

/**
 * Handles each sealed loan result variant with an exhaustive pattern-matching switch.
 */
public final class LoanResultHandler {

    private final Repository<Reservation, String> reservationRepository;
    private final NotificationSender notificationSender;
    private final LibraryEventBus eventBus;
    private final Clock clock;
    private final Consumer<String> logger;

    public LoanResultHandler(
            Repository<Reservation, String> reservationRepository,
            NotificationSender notificationSender,
            LibraryEventBus eventBus,
            Clock clock,
            Consumer<String> logger) {
        this.reservationRepository = Objects.requireNonNull(reservationRepository, "reservationRepository must not be null");
        this.notificationSender = Objects.requireNonNull(notificationSender, "notificationSender must not be null");
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
    }

    public String handle(LoanResult result, Member requester) {
        Objects.requireNonNull(result, "result must not be null");
        return switch (result) {
            case LoanResult.Success success -> handleSuccess(success.loan());
            case LoanResult.InsufficientCopies insufficientCopies -> handleInsufficientCopies(insufficientCopies, requester);
            case LoanResult.MemberSuspended memberSuspended -> handleMemberSuspended(memberSuspended);
            case LoanResult.FineExceeded fineExceeded -> handleFineExceeded(fineExceeded);
        };
    }

    private String handleSuccess(Loan loan) {
        logger.accept("Loan created successfully for " + loan.memberId() + " on " + loan.bookId());
        eventBus.publish(LibraryEvent.of(
                EventType.BOOK_BORROWED,
                Map.of("loan", loan, "memberId", loan.memberId(), "bookId", loan.bookId())));
        return "Borrow succeeded";
    }

    private String handleInsufficientCopies(LoanResult.InsufficientCopies insufficientCopies, Member requester) {
        Objects.requireNonNull(requester, "requester must not be null for insufficient copies");
        Reservation reservation = Reservation.create(
                UUID.randomUUID().toString(),
                insufficientCopies.book().id(),
                requester.id(),
                LocalDate.now(clock));
        reservationRepository.save(reservation);
        notificationSender.send(new Notification(
                UUID.randomUUID().toString(),
                requester.id(),
                requester.notificationAddress(),
                requester.preferredChannel(),
                "Reservation created",
                """
                We could not complete your borrowing request.
                A reservation has been created for %s.
                Requested copies: %d
                Available copies: %d
                """.formatted(insufficientCopies.book().title(), insufficientCopies.requested(), insufficientCopies.available()).trim(),
                LocalDateTime.now(clock)));
        return "Reservation created";
    }

    private String handleMemberSuspended(LoanResult.MemberSuspended memberSuspended) {
        logger.accept("WARNING: member %s is suspended. Reason: %s"
                .formatted(memberSuspended.member().id(), memberSuspended.reason()));
        return "Member suspended";
    }

    private String handleFineExceeded(LoanResult.FineExceeded fineExceeded) {
        notificationSender.send(new Notification(
                UUID.randomUUID().toString(),
                fineExceeded.member().id(),
                fineExceeded.member().notificationAddress(),
                fineExceeded.member().preferredChannel(),
                "Payment reminder",
                """
                Your outstanding fine is above the borrowing threshold.
                Outstanding fine: $%s
                Threshold: $%s
                Please make a payment to continue borrowing.
                """.formatted(fineExceeded.outstandingFine(), fineExceeded.threshold()).trim(),
                LocalDateTime.now(clock)));
        return "Payment reminder sent";
    }
}
