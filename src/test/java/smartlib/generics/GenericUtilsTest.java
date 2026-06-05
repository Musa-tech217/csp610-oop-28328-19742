package smartlib.generics;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;

class GenericUtilsTest {

    @Test
    void safeCastReturnsTypedOptionalWhenCompatible() {
        Book book = new Book("book-1", "Clean Code", "Martin", "ISBN-1", "Programming", 2008, 1);

        Optional<Book> cast = GenericUtils.safeCast(book, Book.class);

        assertTrue(cast.isPresent());
        assertEquals("book-1", cast.orElseThrow().id());
    }

    @Test
    void safeCastReturnsEmptyWhenIncompatible() {
        Optional<Book> cast = GenericUtils.safeCast("not a book", Book.class);

        assertTrue(cast.isEmpty());
    }
}
