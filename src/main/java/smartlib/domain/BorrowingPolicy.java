package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.math.BigDecimal;

/**
 * Encapsulates borrowing rules for a membership type.
 */
public interface BorrowingPolicy {

    MembershipType membershipType();

    int maxBooksAllowed();

    int loanPeriodDays();

    BigDecimal dailyFineAmount();

    default boolean canBorrow(int activeLoanCount) {
        return activeLoanCount < maxBooksAllowed();
    }
}
