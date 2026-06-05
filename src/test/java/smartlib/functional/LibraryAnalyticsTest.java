package smartlib.functional;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.Fine;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.NotificationChannel;

class LibraryAnalyticsTest {

    private LibraryAnalytics analytics;

    @BeforeEach
    void setUp() {
        List<Book> books = List.of(
                new Book("book-1", "Clean Code", "Robert Martin", "ISBN-1", "Programming", 2008, 1),
                new Book("book-2", "Effective Java", "Joshua Bloch", "ISBN-2", "Programming", 2018, 2),
                new Book("book-3", "Domain-Driven Design", "Eric Evans", "ISBN-3", "Design", 2003, 0),
                new Book("book-4", "Modern Java", "Joshua Bloch", "ISBN-4", "Programming", 2021, 1));

        List<Member> members = List.of(
                member("member-1", MembershipType.STANDARD, "0.00"),
                member("member-2", MembershipType.PREMIUM, "0.00"),
                member("member-3", MembershipType.STUDENT, "0.00"));

        List<Loan> loans = List.of(
                Loan.open("loan-1", "book-1", "member-1", LocalDate.of(2026, 1, 10), 14),
                Loan.open("loan-2", "book-2", "member-2", LocalDate.of(2026, 2, 12), 28),
                Loan.open("loan-3", "book-2", "member-3", LocalDate.of(2026, 2, 14), 21),
                Loan.open("loan-4", "book-2", "member-1", LocalDate.of(2026, 3, 20), 14),
                Loan.open("loan-5", "book-3", "member-3", LocalDate.of(2026, 5, 1), 21));

        List<Fine> fines = List.of(
                new Fine("fine-1", "loan-1", "member-1", new BigDecimal("1.00"), LocalDate.of(2026, 1, 30), false),
                new Fine("fine-2", "loan-2", "member-2", new BigDecimal("3.00"), LocalDate.of(2026, 3, 20), false),
                new Fine("fine-3", "loan-3", "member-3", new BigDecimal("2.00"), LocalDate.of(2026, 3, 15), false),
                new Fine("fine-4", "loan-4", "member-1", new BigDecimal("5.00"), LocalDate.of(2026, 4, 10), false));

        Clock clock = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
        analytics = new LibraryAnalytics(books, members, loans, fines, clock);
    }

    @Test
    void topBorrowedBooksUsesGroupingSortingAndLimit() {
        List<LibraryAnalytics.BookBorrowCount> result = analytics.topBorrowedBooks(2);

        assertEquals(2, result.size());
        assertEquals("book-2", result.get(0).book().id());
        assertEquals(3L, result.get(0).borrowCount());
        assertEquals("book-1", result.get(1).book().id());
    }

    @Test
    void overdueMembersFindsMembersWithLoansPastDueToday() {
        List<Member> result = analytics.overdueMembers();

        assertEquals(List.of("member-1", "member-2", "member-3"), result.stream().map(Member::id).toList());
    }

    @Test
    void avgFineByMembershipGroupsAndAveragesByMembershipType() {
        Map<MembershipType, Double> result = analytics.avgFineByMembership();

        assertEquals(3.0, result.get(MembershipType.STANDARD));
        assertEquals(3.0, result.get(MembershipType.PREMIUM));
        assertEquals(2.0, result.get(MembershipType.STUDENT));
    }

    @Test
    void sortedAuthorsReturnsDistinctAlphabeticalAuthorNames() {
        assertEquals(List.of("Eric Evans", "Joshua Bloch", "Robert Martin"), analytics.sortedAuthors());
    }

    @Test
    void partitionByAvailabilityUsesBooleanBuckets() {
        Map<Boolean, List<Book>> partition = analytics.partitionByAvailability();

        assertEquals(List.of("book-1", "book-2", "book-4"),
                partition.get(true).stream().map(Book::id).toList());
        assertEquals(List.of("book-3"), partition.get(false).stream().map(Book::id).toList());
    }

    @Test
    void isbnListForAuthorJoinsSortedIsbnsAndReturnsOptional() {
        assertEquals("ISBN-2, ISBN-4", analytics.isbnListForAuthor("Joshua Bloch").orElseThrow());
        assertTrue(analytics.isbnListForAuthor("Unknown Author").isEmpty());
    }

    @Test
    void loansPerMonthGroupsByMonthAndReturnsSortedMapView() {
        Map<Month, Long> perMonth = analytics.loansPerMonth(2026);

        assertIterableEquals(List.of(Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.MAY), perMonth.keySet());
        assertEquals(2L, perMonth.get(Month.FEBRUARY));
    }

    private Member member(String id, MembershipType type, String fine) {
        return new Member(id, "Member " + id, id + "@example.com", type, new BigDecimal(fine), NotificationChannel.EMAIL);
    }
}
