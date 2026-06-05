package smartlib.domain;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmartLibBusinessRulesTest {

    private BookRepository bookRepository;
    private MemberRepository memberRepository;
    private LoanRepository loanRepository;
    private FineRepository fineRepository;
    private NotificationRepository notificationRepository;
    private DefaultLoanService loanService;
    private BorrowingPolicyRegistry policyRegistry;

    @BeforeEach
    void setUp() {
        bookRepository = new BookRepository();
        memberRepository = new MemberRepository();
        loanRepository = new LoanRepository();
        fineRepository = new FineRepository();
        notificationRepository = new NotificationRepository();
        policyRegistry = new BorrowingPolicyRegistry(List.of(
                new StandardPolicy(),
                new PremiumPolicy(),
                new StudentPolicy()));

        loanService = new DefaultLoanService(
                memberRepository,
                bookRepository,
                loanRepository,
                fineRepository,
                notificationRepository,
                policyRegistry,
                new DefaultFineCalculator(policyRegistry),
                new InMemoryNotificationSender());
    }

    @Test
    void standardMemberMayBorrowUpToThreeBooks() {
        Member member = saveMember("member-1", MembershipType.STANDARD, "0.00");
        saveBooks(4);

        loanService.borrowBook(member.id(), "book-1", LocalDate.of(2026, 5, 1));
        loanService.borrowBook(member.id(), "book-2", LocalDate.of(2026, 5, 1));
        loanService.borrowBook(member.id(), "book-3", LocalDate.of(2026, 5, 1));

        assertThrows(IllegalStateException.class,
                () -> loanService.borrowBook(member.id(), "book-4", LocalDate.of(2026, 5, 1)));
    }

    @Test
    void premiumMemberMayBorrowUpToTenBooks() {
        Member member = saveMember("member-1", MembershipType.PREMIUM, "0.00");
        saveBooks(11);

        for (int i = 1; i <= 10; i++) {
            loanService.borrowBook(member.id(), "book-" + i, LocalDate.of(2026, 5, 1));
        }

        assertThrows(IllegalStateException.class,
                () -> loanService.borrowBook(member.id(), "book-11", LocalDate.of(2026, 5, 1)));
    }

    @Test
    void studentMemberMayBorrowUpToFiveBooks() {
        Member member = saveMember("member-1", MembershipType.STUDENT, "0.00");
        saveBooks(6);

        for (int i = 1; i <= 5; i++) {
            loanService.borrowBook(member.id(), "book-" + i, LocalDate.of(2026, 5, 1));
        }

        assertThrows(IllegalStateException.class,
                () -> loanService.borrowBook(member.id(), "book-6", LocalDate.of(2026, 5, 1)));
    }

    @Test
    void loanDurationDiffersByMembershipType() {
        Loan standardLoan = Loan.open("loan-standard", "book-1", "member-1", LocalDate.of(2026, 5, 1),
                policyRegistry.getPolicyFor(MembershipType.STANDARD).loanPeriodDays());
        Loan premiumLoan = Loan.open("loan-premium", "book-1", "member-2", LocalDate.of(2026, 5, 1),
                policyRegistry.getPolicyFor(MembershipType.PREMIUM).loanPeriodDays());
        Loan studentLoan = Loan.open("loan-student", "book-1", "member-3", LocalDate.of(2026, 5, 1),
                policyRegistry.getPolicyFor(MembershipType.STUDENT).loanPeriodDays());

        assertEquals(LocalDate.of(2026, 5, 15), standardLoan.dueOn());
        assertEquals(LocalDate.of(2026, 5, 29), premiumLoan.dueOn());
        assertEquals(LocalDate.of(2026, 5, 22), studentLoan.dueOn());
    }

    @Test
    void fineIsFiftyCentsPerOverdueDay() {
        DefaultFineCalculator calculator = new DefaultFineCalculator(policyRegistry);
        Member member = saveMember("member-1", MembershipType.STANDARD, "0.00");
        Loan loan = Loan.open("loan-1", "book-1", member.id(), LocalDate.of(2026, 5, 1), 14);

        Fine fine = calculator.calculate(loan, member, LocalDate.of(2026, 5, 18));

        assertEquals(new BigDecimal("1.50"), fine.amount());
    }

    @Test
    void reservationExpiresAfterThreeDays() {
        Reservation reservation = Reservation.create("reservation-1", "book-1", "member-1", LocalDate.of(2026, 5, 1));

        assertEquals(LocalDate.of(2026, 5, 4), reservation.expiresOn());
        assertTrue(reservation.isExpiredOn(LocalDate.of(2026, 5, 5)));
    }

    @Test
    void memberWithOutstandingFineGreaterThanTenDollarsCannotBorrow() {
        Member member = saveMember("member-1", MembershipType.STANDARD, "10.01");
        saveBooks(1);

        assertThrows(IllegalStateException.class,
                () -> loanService.borrowBook(member.id(), "book-1", LocalDate.of(2026, 5, 1)));
    }

    private Member saveMember(String memberId, MembershipType membershipType, String fineAmount) {
        Member member = new Member(
                memberId,
                "Test Member",
                "member@example.com",
                membershipType,
                new BigDecimal(fineAmount),
                NotificationChannel.EMAIL);
        memberRepository.save(member);
        return member;
    }

    private void saveBooks(int count) {
        for (int i = 1; i <= count; i++) {
            bookRepository.save(new Book("book-" + i, "Book " + i, "Author " + i, "ISBN-" + i, 1));
        }
    }
}
