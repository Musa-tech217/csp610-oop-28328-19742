package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable member aggregate root for borrowing decisions.
 */
public record Member(
        String id,
        String name,
        String email,
        MembershipType membershipType,
        BigDecimal outstandingFine,
        NotificationChannel preferredChannel) implements Notifiable {

    private static final BigDecimal MAX_ALLOWED_OUTSTANDING_FINE = new BigDecimal("10.00");

    public Member {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(membershipType, "membershipType must not be null");
        Objects.requireNonNull(outstandingFine, "outstandingFine must not be null");
        Objects.requireNonNull(preferredChannel, "preferredChannel must not be null");
        if (outstandingFine.signum() < 0) {
            throw new IllegalArgumentException("outstandingFine must not be negative");
        }
    }

    public boolean canBorrow(int activeLoanCount, BorrowingPolicy policy) {
        return !hasExceededFineLimit() && policy.canBorrow(activeLoanCount);
    }

    public boolean hasExceededFineLimit() {
        return outstandingFine.compareTo(MAX_ALLOWED_OUTSTANDING_FINE) > 0;
    }

    public Member addFine(BigDecimal additionalFine) {
        Objects.requireNonNull(additionalFine, "additionalFine must not be null");
        return new Member(id, name, email, membershipType, outstandingFine.add(additionalFine), preferredChannel);
    }

    @Override
    public String notificationAddress() {
        return email;
    }
}
