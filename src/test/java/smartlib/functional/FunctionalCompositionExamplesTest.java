package smartlib.functional;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;

class FunctionalCompositionExamplesTest {

    @Test
    void predicateCompositionSupportsAndNegateAndOr() {
        Predicate<Book> availableAndRecent = FunctionalCompositionExamples.availableAndPublishedAfter2010();
        Predicate<Book> unavailableOrClassic = FunctionalCompositionExamples.unavailableOrClassic();

        Book recentAvailable = new Book("book-1", "Modern Java", "Author", "ISBN-1", "Programming", 2021, 1);
        Book classicUnavailable = new Book("book-2", "Legacy Java", "Author", "ISBN-2", "Programming", 2005, 0);
        Book recentUnavailable = new Book("book-3", "Hidden Java", "Author", "ISBN-3", "Programming", 2022, 0);

        assertTrue(availableAndRecent.test(recentAvailable));
        assertFalse(availableAndRecent.test(classicUnavailable));
        assertTrue(unavailableOrClassic.test(classicUnavailable));
        assertTrue(unavailableOrClassic.test(recentUnavailable));
    }

    @Test
    void functionCompositionTransformsRawIsbnStepByStep() {
        Function<String, String> pipeline = FunctionalCompositionExamples.isbnTransformationPipeline();

        assertEquals("BOOK-ISBN123X", pipeline.apply(" isbn-123x "));
    }

    @Test
    void functionCompositionValidatesFormat() {
        Function<String, String> pipeline = FunctionalCompositionExamples.isbnTransformationPipeline();

        assertThrows(IllegalArgumentException.class, () -> pipeline.apply("isbn-123!"));
    }
}
