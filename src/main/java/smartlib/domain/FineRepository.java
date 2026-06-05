package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

public final class FineRepository extends InMemoryRepository<Fine, String> {

    public FineRepository() {
        super(Fine::id);
    }
}
