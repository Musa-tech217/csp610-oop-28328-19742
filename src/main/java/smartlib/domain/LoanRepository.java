package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

public final class LoanRepository extends InMemoryRepository<Loan, String> {

    public LoanRepository() {
        super(Loan::id);
    }
}
