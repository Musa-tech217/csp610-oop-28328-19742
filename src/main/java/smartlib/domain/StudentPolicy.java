package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.math.BigDecimal;

public final class StudentPolicy implements BorrowingPolicy {

    private static final BigDecimal DAILY_FINE = new BigDecimal("0.50");

    @Override
    public MembershipType membershipType() {
        return MembershipType.STUDENT;
    }

    @Override
    public int maxBooksAllowed() {
        return 5;
    }

    @Override
    public int loanPeriodDays() {
        return 21;
    }

    @Override
    public BigDecimal dailyFineAmount() {
        return DAILY_FINE;
    }
}
