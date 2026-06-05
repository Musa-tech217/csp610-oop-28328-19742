package smartlib.patterns.creational;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import smartlib.domain.Book;
import smartlib.domain.BorrowingPolicy;
import smartlib.domain.BorrowingPolicyRegistry;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.PremiumPolicy;
import smartlib.domain.StandardPolicy;
import smartlib.domain.StudentPolicy;

/**
 * Builder for constructing immutable {@link Loan} instances without telescoping constructors.
 */
public final class LoanBuilder {

    private static final BorrowingPolicyRegistry DEFAULT_POLICIES = new BorrowingPolicyRegistry(List.of(
            new StandardPolicy(),
            new PremiumPolicy(),
            new StudentPolicy()));

    private final Member member;
    private final Book book;
    private final BorrowingPolicyRegistry policyRegistry;
    private String notes = "";
    private int renewalCount;
    private String referenceCode;
    private LocalDate borrowedOn = LocalDate.now();
    private String loanId;

    private LoanBuilder(Member member, Book book, BorrowingPolicyRegistry policyRegistry) {
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.book = Objects.requireNonNull(book, "book must not be null");
        this.policyRegistry = Objects.requireNonNull(policyRegistry, "policyRegistry must not be null");
    }

    public static LoanBuilder forMemberAndBook(Member member, Book book) {
        return new LoanBuilder(member, book, DEFAULT_POLICIES);
    }

    public static LoanBuilder forMemberAndBook(Member member, Book book, BorrowingPolicyRegistry policyRegistry) {
        return new LoanBuilder(member, book, policyRegistry);
    }

    public LoanBuilder notes(String notes) {
        this.notes = notes == null ? "" : notes;
        return this;
    }

    public LoanBuilder renewalCount(int renewalCount) {
        if (renewalCount < 0) {
            throw new IllegalArgumentException("renewalCount must be zero or greater");
        }
        this.renewalCount = renewalCount;
        return this;
    }

    public LoanBuilder referenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
        return this;
    }

    public LoanBuilder borrowedOn(LocalDate borrowedOn) {
        this.borrowedOn = Objects.requireNonNull(borrowedOn, "borrowedOn must not be null");
        return this;
    }

    public LoanBuilder loanId(String loanId) {
        this.loanId = loanId;
        return this;
    }

    public Loan build() {
        validate();
        BorrowingPolicy policy = policyRegistry.getPolicyFor(member.membershipType());
        String resolvedLoanId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;
        String resolvedReferenceCode = referenceCode == null || referenceCode.isBlank()
                ? member.id() + "-" + book.id()
                : referenceCode;

        return Loan.open(
                resolvedLoanId,
                book.id(),
                member.id(),
                borrowedOn,
                policy.loanPeriodDays(),
                notes,
                renewalCount,
                resolvedReferenceCode);
    }

    private void validate() {
        if (!book.isAvailableForBorrow()) {
            throw new IllegalStateException("Cannot build a loan for an unavailable book");
        }
    }
}
