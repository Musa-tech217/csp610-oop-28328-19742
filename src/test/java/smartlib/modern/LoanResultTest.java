package smartlib.modern;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.NotificationChannel;

class LoanResultTest {

    @Test
    void allLoanResultVariantsCarryRelevantData() {
        Loan loan = Loan.open("loan-1", "book-1", "member-1", LocalDate.of(2026, 6, 1), 14);
        Book book = new Book("book-1", "Clean Code", "Robert Martin", "ISBN-1", "Programming", 2008, 0);
        Member member = new Member("member-1", "Alice", "alice@example.com", MembershipType.STANDARD,
                new BigDecimal("12.00"), NotificationChannel.EMAIL);

        LoanResult success = new LoanResult.Success(loan);
        LoanResult insufficientCopies = new LoanResult.InsufficientCopies(book, 2, 0);
        LoanResult suspended = new LoanResult.MemberSuspended(member, "Policy review");
        LoanResult fineExceeded = new LoanResult.FineExceeded(member, new BigDecimal("12.00"), new BigDecimal("10.00"));

        assertInstanceOf(LoanResult.Success.class, success);
        assertEquals(loan, ((LoanResult.Success) success).loan());
        assertEquals(0, ((LoanResult.InsufficientCopies) insufficientCopies).available());
        assertEquals("Policy review", ((LoanResult.MemberSuspended) suspended).reason());
        assertEquals(new BigDecimal("10.00"), ((LoanResult.FineExceeded) fineExceeded).threshold());
    }
}
