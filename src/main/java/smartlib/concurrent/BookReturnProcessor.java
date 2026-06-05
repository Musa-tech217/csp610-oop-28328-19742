package smartlib.concurrent;
// Quick note: I kept the shared state small here because that felt safer.

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import smartlib.domain.Book;
import smartlib.domain.Fine;
import smartlib.domain.FineCalculator;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.Notification;
import smartlib.domain.NotificationSender;
import smartlib.domain.Repository;
import smartlib.patterns.behavioural.EventType;
import smartlib.patterns.behavioural.LibraryEvent;
import smartlib.patterns.behavioural.LibraryEventBus;

/**
 * Processes book returns asynchronously while keeping the state transition safe and observable.
 */
public final class BookReturnProcessor {

    private final Repository<Loan, String> loanRepository;
    private final Repository<Book, String> bookRepository;
    private final Repository<Member, String> memberRepository;
    private final Repository<Fine, String> fineRepository;
    private final NotificationSender notificationSender;
    private final LibraryEventBus eventBus;
    private final FineCalculator fineCalculator;
    private final Clock clock;
    private final ExecutorService executorService;
    private final AtomicInteger processedCounter = new AtomicInteger();
    private final AtomicInteger failedCounter = new AtomicInteger();
    private final ReentrantLock stateLock = new ReentrantLock();

    public BookReturnProcessor(
            Repository<Loan, String> loanRepository,
            Repository<Book, String> bookRepository,
            Repository<Member, String> memberRepository,
            Repository<Fine, String> fineRepository,
            NotificationSender notificationSender,
            LibraryEventBus eventBus,
            FineCalculator fineCalculator,
            Clock clock,
            int threadPoolSize) {
        this(
                loanRepository,
                bookRepository,
                memberRepository,
                fineRepository,
                notificationSender,
                eventBus,
                fineCalculator,
                clock,
                Executors.newFixedThreadPool(threadPoolSize, new NamedThreadFactory()));
    }

    BookReturnProcessor(
            Repository<Loan, String> loanRepository,
            Repository<Book, String> bookRepository,
            Repository<Member, String> memberRepository,
            Repository<Fine, String> fineRepository,
            NotificationSender notificationSender,
            LibraryEventBus eventBus,
            FineCalculator fineCalculator,
            Clock clock,
            ExecutorService executorService) {
        this.loanRepository = Objects.requireNonNull(loanRepository, "loanRepository must not be null");
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.fineRepository = Objects.requireNonNull(fineRepository, "fineRepository must not be null");
        this.notificationSender = Objects.requireNonNull(notificationSender, "notificationSender must not be null");
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus must not be null");
        this.fineCalculator = Objects.requireNonNull(fineCalculator, "fineCalculator must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.executorService = Objects.requireNonNull(executorService, "executorService must not be null");
    }

    public CompletableFuture<ReturnResult> processReturn(String loanId) {
        return CompletableFuture
                .supplyAsync(() -> validateAndPrepare(loanId), executorService)
                .thenApplyAsync(this::applyAtomicReturnUpdate, executorService)
                .thenApplyAsync(this::sendNotification, executorService)
                .thenApplyAsync(this::publishReturnEvent, executorService)
                .thenApply(result -> {
                    processedCounter.incrementAndGet();
                    return result;
                })
                .exceptionally(exception -> {
                    failedCounter.incrementAndGet();
                    return ReturnResult.failure(loanId, unwrapMessage(exception));
                });
    }

    public CompletableFuture<List<ReturnResult>> processBatch(List<String> loanIds) {
        Objects.requireNonNull(loanIds, "loanIds must not be null");
        List<CompletableFuture<ReturnResult>> futures = loanIds.stream()
                .map(this::processReturn)
                .toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> futures.stream().map(CompletableFuture::join).toList());
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException interruptedException) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public int processedCount() {
        return processedCounter.get();
    }

    public int failedCount() {
        return failedCounter.get();
    }

    private ReturnPreparation validateAndPrepare(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));
        Member member = memberRepository.findById(loan.memberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + loan.memberId()));
        Book book = bookRepository.findById(loan.bookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + loan.bookId()));
        LocalDate returnedOn = LocalDate.now(clock);
        Loan returnedLoan = loan.returnOn(returnedOn);
        return new ReturnPreparation(loan, returnedLoan, member, book, returnedOn);
    }

    private ReturnState applyAtomicReturnUpdate(ReturnPreparation preparation) {
        stateLock.lock();
        try {
            loanRepository.save(preparation.returnedLoan());
            bookRepository.save(preparation.book().returnCopy());
            Fine fine = fineCalculator.calculate(preparation.originalLoan(), preparation.member(), preparation.returnedOn());
            if (fine.hasAmount()) {
                fineRepository.save(fine);
                memberRepository.save(preparation.member().addFine(fine.amount()));
            }
            return new ReturnState(preparation, fine);
        } finally {
            stateLock.unlock();
        }
    }

    private ReturnState sendNotification(ReturnState state) {
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                state.preparation().member().id(),
                state.preparation().member().notificationAddress(),
                state.preparation().member().preferredChannel(),
                "Book return processed",
                notificationMessage(state),
                LocalDateTime.now(clock));
        notificationSender.send(notification);
        return state;
    }

    private ReturnResult publishReturnEvent(ReturnState state) {
        eventBus.publish(LibraryEvent.of(
                EventType.BOOK_RETURNED,
                Map.of(
                        "loan", state.preparation().originalLoan(),
                        "returnedLoan", state.preparation().returnedLoan(),
                        "member", state.preparation().member(),
                        "returnedOn", state.preparation().returnedOn(),
                        "fine", state.fine())));
        return ReturnResult.success(
                state.preparation().returnedLoan().id(),
                state.preparation().returnedLoan(),
                state.fine().amount());
    }

    private String notificationMessage(ReturnState state) {
        if (state.fine().hasAmount()) {
            return "Loan " + state.preparation().returnedLoan().id()
                    + " returned with fine $" + state.fine().amount();
        }
        return "Loan " + state.preparation().returnedLoan().id() + " returned successfully.";
    }

    private String unwrapMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private record ReturnPreparation(
            Loan originalLoan,
            Loan returnedLoan,
            Member member,
            Book book,
            LocalDate returnedOn) {
    }

    private record ReturnState(ReturnPreparation preparation, Fine fine) {
    }

    private static final class NamedThreadFactory implements ThreadFactory {

        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "book-return-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
