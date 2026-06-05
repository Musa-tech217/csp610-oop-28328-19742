package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.time.LocalDate;

/**
 * Coordinates borrowing and returning books.
 */
public interface LoanService {

    Loan borrowBook(String memberId, String bookId, LocalDate borrowedOn);

    Loan returnBook(String loanId, LocalDate returnedOn);
}
