package smartlib.modern;
// Quick note: I used the newer Java feature here because it made the intent clearer.

import java.math.BigDecimal;
import smartlib.domain.Book;
import smartlib.domain.Loan;
import smartlib.domain.Member;

/**
 * Sealed result hierarchy for borrow attempts.
 */
public sealed interface LoanResult
        permits LoanResult.Success, LoanResult.InsufficientCopies, LoanResult.MemberSuspended, LoanResult.FineExceeded {

    record Success(Loan loan) implements LoanResult {
    }

    record InsufficientCopies(Book book, int requested, int available) implements LoanResult {
    }

    record MemberSuspended(Member member, String reason) implements LoanResult {
    }

    record FineExceeded(Member member, BigDecimal outstandingFine, BigDecimal threshold) implements LoanResult {
    }
}
