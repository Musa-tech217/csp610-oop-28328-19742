package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable loan entity that models lifecycle transitions by returning new instances.
 */
public record Loan(
        String id,
        String bookId,
        String memberId,
        LocalDate borrowedOn,
        LocalDate dueOn,
        LoanStatus status,
        LocalDate returnedOn,
        String notes,
        int renewalCount,
        String referenceCode) {

    public Loan {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(bookId, "bookId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(borrowedOn, "borrowedOn must not be null");
        Objects.requireNonNull(dueOn, "dueOn must not be null");
        Objects.requireNonNull(status, "status must not be null");
        notes = notes == null ? "" : notes;
        referenceCode = referenceCode == null ? "" : referenceCode;
        if (renewalCount < 0) {
            throw new IllegalArgumentException("renewalCount must be zero or greater");
        }
        if (dueOn.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("dueOn must be on or after borrowedOn");
        }
    }

    public static Loan open(String id, String bookId, String memberId, LocalDate borrowedOn, int loanPeriodDays) {
        return open(id, bookId, memberId, borrowedOn, loanPeriodDays, "", 0, "");
    }

    public static Loan open(
            String id,
            String bookId,
            String memberId,
            LocalDate borrowedOn,
            int loanPeriodDays,
            String notes,
            int renewalCount,
            String referenceCode) {
        Objects.requireNonNull(borrowedOn, "borrowedOn must not be null");
        return new Loan(
                id,
                bookId,
                memberId,
                borrowedOn,
                borrowedOn.plusDays(loanPeriodDays),
                LoanStatus.ACTIVE,
                null,
                notes,
                renewalCount,
                referenceCode);
    }

    public boolean isActive() {
        return status == LoanStatus.ACTIVE;
    }

    public boolean isOverdueOn(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return isActive() && date.isAfter(dueOn);
    }

    public Loan returnOn(LocalDate returnedOn) {
        Objects.requireNonNull(returnedOn, "returnedOn must not be null");
        if (!isActive()) {
            throw new IllegalStateException("Only active loans can be returned");
        }
        if (returnedOn.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("returnedOn must not be before borrowedOn");
        }
        return new Loan(id, bookId, memberId, borrowedOn, dueOn, LoanStatus.RETURNED, returnedOn, notes, renewalCount,
                referenceCode);
    }
}
