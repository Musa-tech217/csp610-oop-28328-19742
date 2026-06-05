package smartlib.functional;
// Quick note: this part reads better to me as a stream pipeline than a loop.

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import smartlib.domain.Book;

/**
 * Predicate and function composition examples for SmartLib.
 */
public final class FunctionalCompositionExamples {

    private FunctionalCompositionExamples() {
    }

    public static Predicate<Book> availableAndPublishedAfter2010() {
        Predicate<Book> available = Book::isAvailableForBorrow;
        Predicate<Book> publishedAfter2010 = book -> book.publicationYear() > 2010;
        return available.and(publishedAfter2010);
    }

    public static Predicate<Book> unavailableOrClassic() {
        Predicate<Book> available = Book::isAvailableForBorrow;
        Predicate<Book> classic = book -> book.publicationYear() <= 2010;
        return available.negate().or(classic);
    }

    public static Function<String, String> isbnTransformationPipeline() {
        Function<String, String> trim = String::trim;
        Function<String, String> removeSeparators = value -> value.replace("-", "").replace(" ", "");
        Function<String, String> uppercase = String::toUpperCase;
        Function<String, String> validate = value -> {
            if (!value.matches("[A-Z0-9]+")) {
                throw new IllegalArgumentException("ISBN contains invalid characters");
            }
            return value;
        };
        Function<String, String> prefix = value -> "BOOK-" + value;

        return uppercase
                .compose(removeSeparators)
                .compose(trim)
                .andThen(validate)
                .andThen(prefix);
    }
}
