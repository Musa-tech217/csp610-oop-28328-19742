package smartlib.functional;
// Quick note: this part reads better to me as a stream pipeline than a loop.

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;

/**
 * Custom collector that summarises loans using mutable accumulation and an immutable final result.
 */
public final class LoanSummaryCollector implements Collector<Loan, LoanSummaryCollector.MutableLoanSummary, LoanSummary> {

    private final Function<? super Loan, ? extends Member> memberResolver;
    private final Function<? super Loan, BigDecimal> outstandingFineResolver;

    public LoanSummaryCollector(
            Function<? super Loan, ? extends Member> memberResolver,
            Function<? super Loan, BigDecimal> outstandingFineResolver) {
        this.memberResolver = memberResolver;
        this.outstandingFineResolver = outstandingFineResolver;
    }

    @Override
    public Supplier<MutableLoanSummary> supplier() {
        return MutableLoanSummary::new;
    }

    @Override
    public java.util.function.BiConsumer<MutableLoanSummary, Loan> accumulator() {
        return (summary, loan) -> {
            summary.totalLoanCount++;
            summary.totalOutstandingFines = summary.totalOutstandingFines.add(outstandingFineResolver.apply(loan));
            summary.latestLoanDate = summary.latestLoanDate == null
                    ? loan.borrowedOn()
                    : summary.latestLoanDate.isAfter(loan.borrowedOn()) ? summary.latestLoanDate : loan.borrowedOn();
            MembershipType membershipType = memberResolver.apply(loan).membershipType();
            summary.membershipBreakdown.merge(membershipType, 1L, Long::sum);
        };
    }

    @Override
    public java.util.function.BinaryOperator<MutableLoanSummary> combiner() {
        return (left, right) -> {
            left.totalLoanCount += right.totalLoanCount;
            left.totalOutstandingFines = left.totalOutstandingFines.add(right.totalOutstandingFines);
            if (left.latestLoanDate == null || (right.latestLoanDate != null && right.latestLoanDate.isAfter(left.latestLoanDate))) {
                left.latestLoanDate = right.latestLoanDate;
            }
            right.membershipBreakdown.forEach((type, count) -> left.membershipBreakdown.merge(type, count, Long::sum));
            return left;
        };
    }

    @Override
    public java.util.function.Function<MutableLoanSummary, LoanSummary> finisher() {
        return summary -> new LoanSummary(
                summary.totalLoanCount,
                summary.totalOutstandingFines,
                summary.latestLoanDate,
                Map.copyOf(summary.membershipBreakdown));
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }

    static final class MutableLoanSummary {

        private long totalLoanCount;
        private BigDecimal totalOutstandingFines = BigDecimal.ZERO;
        private LocalDate latestLoanDate;
        private final Map<MembershipType, Long> membershipBreakdown = new EnumMap<>(MembershipType.class);
    }
}
