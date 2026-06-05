package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

public final class BookRepository extends InMemoryRepository<Book, String> {

    public BookRepository() {
        super(Book::id);
    }
}
