package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

public final class ReservationRepository extends InMemoryRepository<Reservation, String> {

    public ReservationRepository() {
        super(Reservation::id);
    }
}
