package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.util.Comparator;
import java.util.Objects;

/**
 * Immutable representation of a book and its current available copy count.
 */
public record Book(
        String id,
        String title,
        String author,
        String isbn,
        String genre,
        int publicationYear,
        int availableCopies) implements Borrowable, Comparable<Book> {

    private static final Comparator<Book> NATURAL_ORDER = Comparator
            .comparing(Book::title)
            .thenComparing(Book::author)
            .thenComparingInt(Book::publicationYear)
            .thenComparing(Book::isbn);

    public Book(String id, String title, String author, String isbn, int availableCopies) {
        this(id, title, author, isbn, "Unknown", 0, availableCopies);
    }

    public Book {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(author, "author must not be null");
        Objects.requireNonNull(isbn, "isbn must not be null");
        Objects.requireNonNull(genre, "genre must not be null");
        if (publicationYear < 0) {
            throw new IllegalArgumentException("publicationYear must be zero or greater");
        }
        if (availableCopies < 0) {
            throw new IllegalArgumentException("availableCopies must be zero or greater");
        }
    }

    @Override
    public boolean isAvailableForBorrow() {
        return availableCopies > 0;
    }

    public Book borrowCopy() {
        if (!isAvailableForBorrow()) {
            throw new IllegalStateException("Book is not available for borrowing");
        }
        return new Book(id, title, author, isbn, genre, publicationYear, availableCopies - 1);
    }

    public Book returnCopy() {
        return new Book(id, title, author, isbn, genre, publicationYear, availableCopies + 1);
    }

    @Override
    public int compareTo(Book other) {
        Objects.requireNonNull(other, "other must not be null");
        return NATURAL_ORDER.compare(this, other);
    }
}
