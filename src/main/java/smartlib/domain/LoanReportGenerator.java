package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.util.List;
import java.util.stream.Collectors;

/**
 * Separate report concern to demonstrate that reporting does not belong inside loan management.
 */
public final class LoanReportGenerator implements ReportGenerator<Loan> {

    @Override
    public String generateReport(List<Loan> loans) {
        return loans.stream()
                .map(loan -> loan.id() + ":" + loan.status())
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
