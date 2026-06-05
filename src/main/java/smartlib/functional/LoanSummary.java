package smartlib.functional;
// Quick note: this part reads better to me as a stream pipeline than a loop.

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import smartlib.domain.MembershipType;

/**
 * Immutable result of summarising a stream of loans.
 */
public record LoanSummary(
        long totalLoanCount,
        BigDecimal totalOutstandingFines,
        LocalDate latestLoanDate,
        Map<MembershipType, Long> membershipBreakdown) {
}
