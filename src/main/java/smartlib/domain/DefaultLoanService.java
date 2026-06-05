package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Orchestrates borrowing and returns while delegating pricing, persistence, and notifications.
 */
public final class DefaultLoanService implements LoanService {

    private final Repository<Member, String> memberRepository;
    private final Repository<Book, String> bookRepository;
    private final Repository<Loan, String> loanRepository;
    private final Repository<Fine, String> fineRepository;
    private final Repository<Notification, String> notificationRepository;
    private final BorrowingPolicyRegistry policyRegistry;
    private final FineCalculator fineCalculator;
    private final NotificationSender notificationSender;

    public DefaultLoanService(
            Repository<Member, String> memberRepository,
            Repository<Book, String> bookRepository,
            Repository<Loan, String> loanRepository,
            Repository<Fine, String> fineRepository,
            Repository<Notification, String> notificationRepository,
            BorrowingPolicyRegistry policyRegistry,
            FineCalculator fineCalculator,
            NotificationSender notificationSender) {
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository must not be null");
        this.loanRepository = Objects.requireNonNull(loanRepository, "loanRepository must not be null");
        this.fineRepository = Objects.requireNonNull(fineRepository, "fineRepository must not be null");
        this.notificationRepository = Objects.requireNonNull(notificationRepository, "notificationRepository must not be null");
        this.policyRegistry = Objects.requireNonNull(policyRegistry, "policyRegistry must not be null");
        this.fineCalculator = Objects.requireNonNull(fineCalculator, "fineCalculator must not be null");
        this.notificationSender = Objects.requireNonNull(notificationSender, "notificationSender must not be null");
    }

    @Override
    public Loan borrowBook(String memberId, String bookId, LocalDate borrowedOn) {
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(bookId, "bookId must not be null");
        Objects.requireNonNull(borrowedOn, "borrowedOn must not be null");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        BorrowingPolicy policy = policyRegistry.getPolicyFor(member.membershipType());
        int activeLoanCount = (int) loanRepository.findAll().stream()
                .filter(loan -> loan.memberId().equals(memberId))
                .filter(Loan::isActive)
                .count();

        if (!member.canBorrow(activeLoanCount, policy)) {
            throw new IllegalStateException("Member is not allowed to borrow more books");
        }
        if (!book.isAvailableForBorrow()) {
            throw new IllegalStateException("Book is not available for borrowing");
        }

        bookRepository.save(book.borrowCopy());
        Loan loan = Loan.open(UUID.randomUUID().toString(), book.id(), member.id(), borrowedOn, policy.loanPeriodDays());
        return loanRepository.save(loan);
    }

    @Override
    public Loan returnBook(String loanId, LocalDate returnedOn) {
        Objects.requireNonNull(loanId, "loanId must not be null");
        Objects.requireNonNull(returnedOn, "returnedOn must not be null");

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));
        Member member = memberRepository.findById(loan.memberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + loan.memberId()));
        Book book = bookRepository.findById(loan.bookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + loan.bookId()));

        Loan returnedLoan = loan.returnOn(returnedOn);
        loanRepository.save(returnedLoan);
        bookRepository.save(book.returnCopy());

        Fine fine = fineCalculator.calculate(loan, member, returnedOn);
        if (fine.hasAmount()) {
            fineRepository.save(fine);
            memberRepository.save(member.addFine(fine.amount()));
        }

        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                member.id(),
                member.notificationAddress(),
                member.preferredChannel(),
                "Book return processed",
                createReturnMessage(returnedLoan, fine.amount()),
                LocalDateTime.now());
        notificationRepository.save(notification);
        notificationSender.send(notification);

        return returnedLoan;
    }

    private String createReturnMessage(Loan returnedLoan, BigDecimal fineAmount) {
        if (fineAmount.signum() > 0) {
            return "Loan " + returnedLoan.id() + " has been returned. Outstanding fine: $" + fineAmount;
        }
        return "Loan " + returnedLoan.id() + " has been returned with no fine due.";
    }
}
