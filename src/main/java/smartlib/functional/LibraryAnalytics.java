package smartlib.functional;
// Quick note: this part reads better to me as a stream pipeline than a loop.

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import smartlib.domain.Book;
import smartlib.domain.Fine;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;

/**
 * Stream-based analytics over SmartLib domain data.
 */
public final class LibraryAnalytics {

    private final List<Book> books;
    private final List<Member> members;
    private final List<Loan> loans;
    private final List<Fine> fines;
    private final Clock clock;

    public LibraryAnalytics(
            Collection<Book> books,
            Collection<Member> members,
            Collection<Loan> loans,
            Collection<Fine> fines,
            Clock clock) {
        this.books = List.copyOf(Objects.requireNonNull(books, "books must not be null"));
        this.members = List.copyOf(Objects.requireNonNull(members, "members must not be null"));
        this.loans = List.copyOf(Objects.requireNonNull(loans, "loans must not be null"));
        this.fines = List.copyOf(Objects.requireNonNull(fines, "fines must not be null"));
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public List<BookBorrowCount> topBorrowedBooks(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative");
        }
        Map<String, Book> booksById = books.stream()
                .collect(Collectors.toUnmodifiableMap(Book::id, Function.identity()));

        return loans.stream()
                .collect(Collectors.groupingBy(Loan::bookId, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> booksById.containsKey(entry.getKey()))
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(n)
                .map(entry -> new BookBorrowCount(booksById.get(entry.getKey()), entry.getValue()))
                .toList();
    }

    public List<Member> overdueMembers() {
        LocalDate today = LocalDate.now(clock);
        Set<String> overdueMemberIds = loans.stream()
                .filter(loan -> loan.isOverdueOn(today))
                .map(Loan::memberId)
                .collect(Collectors.toUnmodifiableSet());

        return members.stream()
                .filter(member -> overdueMemberIds.contains(member.id()))
                .toList();
    }

    public Map<MembershipType, Double> avgFineByMembership() {
        Map<String, MembershipType> membershipByMemberId = members.stream()
                .collect(Collectors.toUnmodifiableMap(Member::id, Member::membershipType));

        return fines.stream()
                .filter(fine -> membershipByMemberId.containsKey(fine.memberId()))
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(
                                fine -> membershipByMemberId.get(fine.memberId()),
                                Collectors.averagingDouble(fine -> fine.amount().doubleValue())),
                        Map::copyOf));
    }

    public List<String> sortedAuthors() {
        return books.stream()
                .map(Book::author)
                .distinct()
                .sorted()
                .toList();
    }

    public Map<Boolean, List<Book>> partitionByAvailability() {
        return books.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.partitioningBy(
                                Book::isAvailableForBorrow,
                                Collectors.collectingAndThen(Collectors.toList(), List::copyOf)),
                        Map::copyOf));
    }

    public Optional<String> isbnListForAuthor(String author) {
        Objects.requireNonNull(author, "author must not be null");
        String joined = books.stream()
                .filter(book -> book.author().equals(author))
                .map(Book::isbn)
                .sorted()
                .collect(Collectors.joining(", "));
        return joined.isBlank() ? Optional.empty() : Optional.of(joined);
    }

    public Map<Month, Long> loansPerMonth(int year) {
        Map<Month, Long> sortedByMonth = loans.stream()
                .filter(loan -> loan.borrowedOn().getYear() == year)
                .collect(Collectors.groupingBy(
                        loan -> loan.borrowedOn().getMonth(),
                        () -> new java.util.TreeMap<>(),
                        Collectors.counting()));

        return Collections.unmodifiableMap(sortedByMonth.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new)));
    }

    public record BookBorrowCount(Book book, long borrowCount) {
    }
}
