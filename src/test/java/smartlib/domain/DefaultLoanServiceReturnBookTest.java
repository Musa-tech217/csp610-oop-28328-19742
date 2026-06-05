package smartlib.domain;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultLoanServiceReturnBookTest {

    @Test
    void returnBookDelegatesResponsibilitiesToTheRightCollaborators() {
        @SuppressWarnings("unchecked")
        Repository<Member, String> memberRepository = mock(Repository.class);
        @SuppressWarnings("unchecked")
        Repository<Book, String> bookRepository = mock(Repository.class);
        @SuppressWarnings("unchecked")
        Repository<Loan, String> loanRepository = mock(Repository.class);
        @SuppressWarnings("unchecked")
        Repository<Fine, String> fineRepository = mock(Repository.class);
        @SuppressWarnings("unchecked")
        Repository<Notification, String> notificationRepository = mock(Repository.class);

        FineCalculator fineCalculator = mock(FineCalculator.class);
        NotificationSender notificationSender = mock(NotificationSender.class);
        BorrowingPolicyRegistry policyRegistry = new BorrowingPolicyRegistry(List.of(
                new StandardPolicy(),
                new PremiumPolicy(),
                new StudentPolicy()));

        DefaultLoanService loanService = new DefaultLoanService(
                memberRepository,
                bookRepository,
                loanRepository,
                fineRepository,
                notificationRepository,
                policyRegistry,
                fineCalculator,
                notificationSender);

        Loan activeLoan = Loan.open("loan-1", "book-1", "member-1", LocalDate.of(2026, 5, 1), 14);
        Loan returnedLoan = activeLoan.returnOn(LocalDate.of(2026, 5, 20));
        Member member = new Member(
                "member-1",
                "Test Member",
                "member@example.com",
                MembershipType.STANDARD,
                BigDecimal.ZERO,
                NotificationChannel.EMAIL);
        Book book = new Book("book-1", "Book 1", "Author 1", "ISBN-1", 0);
        Fine fine = new Fine("fine-loan-1", "loan-1", "member-1", new BigDecimal("2.50"),
                LocalDate.of(2026, 5, 20), false);

        when(loanRepository.findById("loan-1")).thenReturn(Optional.of(activeLoan));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bookRepository.findById("book-1")).thenReturn(Optional.of(book));
        when(loanRepository.save(any(Loan.class))).thenReturn(returnedLoan);
        when(fineCalculator.calculate(activeLoan, member, LocalDate.of(2026, 5, 20))).thenReturn(fine);

        Loan result = loanService.returnBook("loan-1", LocalDate.of(2026, 5, 20));

        assertNotNull(result);
        assertEquals(returnedLoan, result);
        assertEquals(LoanStatus.RETURNED, result.status());
        verify(loanRepository).save(eq(returnedLoan));
        verify(fineCalculator).calculate(activeLoan, member, LocalDate.of(2026, 5, 20));
        verify(fineRepository).save(fine);
        verify(memberRepository).save(member.addFine(new BigDecimal("2.50")));
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(notificationSender, times(1)).send(any(Notification.class));
        verify(bookRepository).save(book.returnCopy());
    }
}
