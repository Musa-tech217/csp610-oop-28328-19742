package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Fine calculation is isolated here so loan orchestration does not own pricing rules.
 */
public final class DefaultFineCalculator implements FineCalculator {

    private final BorrowingPolicyRegistry policyRegistry;

    public DefaultFineCalculator(BorrowingPolicyRegistry policyRegistry) {
        this.policyRegistry = Objects.requireNonNull(policyRegistry, "policyRegistry must not be null");
    }

    @Override
    public Fine calculate(Loan loan, Member member, LocalDate returnedOn) {
        Objects.requireNonNull(loan, "loan must not be null");
        Objects.requireNonNull(member, "member must not be null");
        Objects.requireNonNull(returnedOn, "returnedOn must not be null");

        BorrowingPolicy policy = policyRegistry.getPolicyFor(member.membershipType());
        long overdueDays = Math.max(0L, ChronoUnit.DAYS.between(loan.dueOn(), returnedOn));
        BigDecimal amount = policy.dailyFineAmount().multiply(BigDecimal.valueOf(overdueDays));
        return new Fine("fine-" + loan.id(), loan.id(), member.id(), amount, returnedOn, false);
    }
}
