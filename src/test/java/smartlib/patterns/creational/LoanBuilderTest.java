package smartlib.patterns.creational;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.NotificationChannel;

class LoanBuilderTest {

    @Test
    void buildsMinimalLoanWithSensibleDefaults() {
        Member member = member(MembershipType.STANDARD);
        Book book = book();

        var loan = LoanBuilder.forMemberAndBook(member, book)
                .borrowedOn(LocalDate.of(2026, 6, 1))
                .build();

        assertNotNull(loan.id());
        assertEquals(member.id(), loan.memberId());
        assertEquals(book.id(), loan.bookId());
        assertEquals(LocalDate.of(2026, 6, 15), loan.dueOn());
        assertEquals("", loan.notes());
        assertEquals(0, loan.renewalCount());
        assertEquals(member.id() + "-" + book.id(), loan.referenceCode());
        assertFalse(loan.referenceCode().isBlank());
    }

    @Test
    void buildsFullyConfiguredLoan() {
        Member member = member(MembershipType.PREMIUM);
        Book book = book();

        var loan = LoanBuilder.forMemberAndBook(member, book)
                .loanId("loan-42")
                .borrowedOn(LocalDate.of(2026, 6, 1))
                .notes("Handle with care")
                .renewalCount(2)
                .referenceCode("REF-2026-42")
                .build();

        assertEquals("loan-42", loan.id());
        assertEquals(LocalDate.of(2026, 6, 29), loan.dueOn());
        assertEquals("Handle with care", loan.notes());
        assertEquals(2, loan.renewalCount());
        assertEquals("REF-2026-42", loan.referenceCode());
    }

    @Test
    void rejectsNegativeRenewalCount() {
        Member member = member(MembershipType.STUDENT);
        Book book = book();

        assertThrows(IllegalArgumentException.class, () -> LoanBuilder.forMemberAndBook(member, book)
                .renewalCount(-1));
    }

    private Member member(MembershipType membershipType) {
        return new Member(
                "member-1",
                "Test Member",
                "member@example.com",
                membershipType,
                BigDecimal.ZERO,
                NotificationChannel.EMAIL);
    }

    private Book book() {
        return new Book("book-1", "Patterns", "Gamma", "ISBN-1", 1);
    }
}
