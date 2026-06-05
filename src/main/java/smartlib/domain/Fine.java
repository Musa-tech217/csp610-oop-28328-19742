package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable fine value capturing amount and payment state.
 */
public record Fine(
        String id,
        String loanId,
        String memberId,
        BigDecimal amount,
        LocalDate assessedOn,
        boolean paid) {

    public Fine {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(loanId, "loanId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(assessedOn, "assessedOn must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    public static Fine none(String loanId, String memberId, LocalDate assessedOn) {
        return new Fine("fine-" + loanId, loanId, memberId, BigDecimal.ZERO, assessedOn, false);
    }

    public boolean hasAmount() {
        return amount.signum() > 0;
    }

    public Fine markPaid() {
        return new Fine(id, loanId, memberId, amount, assessedOn, true);
    }
}
