package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.time.LocalDate;

/**
 * Calculates fines independently from loan orchestration.
 */
public interface FineCalculator {

    Fine calculate(Loan loan, Member member, LocalDate returnedOn);
}
