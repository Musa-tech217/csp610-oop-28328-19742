package smartlib.generics;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.Borrowable;

class CatalogueTest {

    @Test
    void searchFindsMatchingBooksByPredicate() {
        Catalogue<Book> catalogue = new Catalogue<>();
        catalogue.add(book("book-1", "Domain-Driven Design", "Design", 2003, 1));
        catalogue.add(book("book-2", "Clean Code", "Programming", 2008, 1));
        catalogue.add(book("book-3", "Refactoring", "Programming", 1999, 0));

        List<Book> result = catalogue.search(book -> book.genre().equals("Programming"));

        assertEquals(List.of("book-2", "book-3"), result.stream().map(Book::id).toList());
    }

    @Test
    void sortedViewsCanUseFlexibleKeyExtractors() {
        Catalogue<Book> catalogue = new Catalogue<>();
        catalogue.add(book("book-1", "Refactoring", "Programming", 1999, 1));
        catalogue.add(book("book-2", "Clean Code", "Programming", 2008, 1));
        catalogue.add(book("book-3", "Domain-Driven Design", "Design", 2003, 1));

        List<Book> sorted = catalogue.sortedBy(Book::publicationYear);

        assertEquals(List.of("book-1", "book-3", "book-2"), sorted.stream().map(Book::id).toList());
    }

    @Test
    void groupCountBuildsGenreFrequencyMap() {
        Catalogue<Book> catalogue = new Catalogue<>();
        catalogue.add(book("book-1", "Clean Code", "Programming", 2008, 1));
        catalogue.add(book("book-2", "Refactoring", "Programming", 1999, 1));
        catalogue.add(book("book-3", "The Design of Everyday Things", "Design", 1988, 1));

        Map<String, Long> genreFrequency = catalogue.groupCount(Book::genre);

        assertEquals(Map.of("Programming", 2L, "Design", 1L), genreFrequency);
    }

    @Test
    void groupCountBuildsYearlyPublicationHistogram() {
        Catalogue<Book> catalogue = new Catalogue<>();
        catalogue.add(book("book-1", "Patterns", "Software", 1994, 1));
        catalogue.add(book("book-2", "Refactoring", "Programming", 1999, 1));
        catalogue.add(book("book-3", "Another Refactoring", "Programming", 1999, 1));

        Map<Integer, Long> histogram = catalogue.groupCount(Book::publicationYear);

        assertEquals(1L, histogram.get(1994));
        assertEquals(2L, histogram.get(1999));
    }

    @Test
    void addAllAvailableUsesProducerExtendsCorrectly() {
        Catalogue<Book> destination = new Catalogue<>();
        Catalogue<Book> source = new Catalogue<>();
        source.add(book("book-1", "Available Book", "Programming", 2020, 1));
        source.add(book("book-2", "Unavailable Book", "Programming", 2021, 0));

        destination.addAllAvailable(source);

        assertEquals(1, destination.items().size());
        assertEquals("book-1", destination.items().get(0).id());
    }

    @Test
    void updateMatchingUsesConsumerSuperForMutableItems() {
        Catalogue<MutableBookCopy> catalogue = new Catalogue<>();
        MutableBookCopy first = new MutableBookCopy("copy-1", true);
        MutableBookCopy second = new MutableBookCopy("copy-2", false);
        catalogue.add(first);
        catalogue.add(second);

        ConsumerTracker tracker = new ConsumerTracker();
        catalogue.updateMatching(MutableBookCopy::isAvailableForBorrow, tracker::markProcessed);

        assertTrue(first.processed());
        assertEquals(1, tracker.processedCount().get());
    }

    @Test
    void bookHasNaturalComparableOrdering() {
        List<Book> books = List.of(
                book("book-2", "Refactoring", "Programming", 1999, 1),
                book("book-1", "Clean Code", "Programming", 2008, 1));

        List<Book> sorted = books.stream().sorted().toList();

        assertIterableEquals(List.of("book-1", "book-2"), sorted.stream().map(Book::id).toList());
    }

    @Test
    void searchResultsAreImmutable() {
        Catalogue<Book> catalogue = new Catalogue<>();
        catalogue.add(book("book-1", "Clean Code", "Programming", 2008, 1));

        List<Book> searchResult = catalogue.search(book -> true);

        assertThrows(UnsupportedOperationException.class, () -> searchResult.add(book("book-2", "X", "Y", 2020, 1)));
    }

    private Book book(String id, String title, String genre, int publicationYear, int availableCopies) {
        return new Book(id, title, "Author " + id, "ISBN-" + id, genre, publicationYear, availableCopies);
    }
    private static final class MutableBookCopy implements Borrowable, Comparable<MutableBookCopy> {

        private final String id;
        private final boolean available;
        private boolean processed;

        private MutableBookCopy(String id, boolean available) {
            this.id = id;
            this.available = available;
        }

        boolean processed() {
            return processed;
        }

        void processed(boolean processed) {
            this.processed = processed;
        }

        @Override
        public boolean isAvailableForBorrow() {
            return available;
        }

        @Override
        public int compareTo(MutableBookCopy other) {
            return this.id.compareTo(other.id);
        }
    }

    private static final class ConsumerTracker {

        private final AtomicInteger processedCount = new AtomicInteger();

        void markProcessed(Object candidate) {
            if (candidate instanceof MutableBookCopy copy) {
                copy.processed(true);
                processedCount.incrementAndGet();
            }
        }

        AtomicInteger processedCount() {
            return processedCount;
        }
    }
}
