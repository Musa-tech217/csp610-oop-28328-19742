package smartlib.functional;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.NotificationChannel;

class LoanSummaryCollectorTest {

    private List<Loan> loans;
    private Map<String, Member> membersById;
    private Map<String, BigDecimal> fineByLoanId;
    private LoanSummaryCollector collector;

    @BeforeEach
    void setUp() {
        loans = List.of(
                Loan.open("loan-1", "book-1", "member-1", LocalDate.of(2026, 1, 10), 14),
                Loan.open("loan-2", "book-2", "member-2", LocalDate.of(2026, 4, 12), 28),
                Loan.open("loan-3", "book-3", "member-1", LocalDate.of(2026, 6, 5), 14));

        membersById = Map.of(
                "member-1", member("member-1", MembershipType.STANDARD),
                "member-2", member("member-2", MembershipType.PREMIUM));

        fineByLoanId = Map.of(
                "loan-1", new BigDecimal("1.50"),
                "loan-2", new BigDecimal("0.00"),
                "loan-3", new BigDecimal("2.00"));

        collector = new LoanSummaryCollector(
                loan -> membersById.get(loan.memberId()),
                loan -> fineByLoanId.getOrDefault(loan.id(), BigDecimal.ZERO));
    }

    @Test
    void sequentialStreamCollectionProducesExpectedSummary() {
        LoanSummary summary = loans.stream().collect(collector);

        assertEquals(3L, summary.totalLoanCount());
        assertEquals(new BigDecimal("3.50"), summary.totalOutstandingFines());
    }

    @Test
    void parallelStreamCollectionProducesExpectedSummary() {
        LoanSummary summary = loans.parallelStream().collect(collector);

        assertEquals(3L, summary.totalLoanCount());
        assertEquals(new BigDecimal("3.50"), summary.totalOutstandingFines());
    }

    @Test
    void membershipBreakdownCountsLoansPerMembershipType() {
        LoanSummary summary = loans.stream().collect(collector);

        assertEquals(2L, summary.membershipBreakdown().get(MembershipType.STANDARD));
        assertEquals(1L, summary.membershipBreakdown().get(MembershipType.PREMIUM));
    }

    @Test
    void latestLoanDateTracksMostRecentBorrowedOnValue() {
        LoanSummary summary = loans.stream().collect(collector);

        assertEquals(LocalDate.of(2026, 6, 5), summary.latestLoanDate());
    }

    private Member member(String id, MembershipType membershipType) {
        return new Member(id, "Member " + id, id + "@example.com", membershipType, BigDecimal.ZERO, NotificationChannel.EMAIL);
    }
}
