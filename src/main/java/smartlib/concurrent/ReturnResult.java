package smartlib.concurrent;
// Quick note: I kept the shared state small here because that felt safer.

import java.math.BigDecimal;
import smartlib.domain.Loan;

/**
 * Immutable result of an asynchronous return attempt.
 */
public record ReturnResult(
        String loanId,
        boolean success,
        String message,
        Loan returnedLoan,
        BigDecimal fineAmount) {

    public static ReturnResult success(String loanId, Loan returnedLoan, BigDecimal fineAmount) {
        return new ReturnResult(loanId, true, "Return processed successfully", returnedLoan, fineAmount);
    }

    public static ReturnResult failure(String loanId, String message) {
        return new ReturnResult(loanId, false, message, null, BigDecimal.ZERO);
    }
}
