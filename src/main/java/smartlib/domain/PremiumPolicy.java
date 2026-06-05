package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.
import java.math.BigDecimal;

public final class PremiumPolicy implements BorrowingPolicy {

    private static final BigDecimal DAILY_FINE = new BigDecimal("0.50");

    @Override
    public MembershipType membershipType() {
        return MembershipType.PREMIUM;
    }

    @Override
    public int maxBooksAllowed() {
        return 10;
    }

    @Override
    public int loanPeriodDays() {
        return 28;
    }

    @Override
    public BigDecimal dailyFineAmount() {
        return DAILY_FINE;
    }
}
