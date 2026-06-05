package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable reservation with a fixed three-day expiry rule.
 */
public record Reservation(
        String id,
        String bookId,
        String memberId,
        LocalDate reservedOn,
        LocalDate expiresOn,
        ReservationStatus status) {

    private static final int EXPIRY_DAYS = 3;

    public Reservation {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(bookId, "bookId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(reservedOn, "reservedOn must not be null");
        Objects.requireNonNull(expiresOn, "expiresOn must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (expiresOn.isBefore(reservedOn)) {
            throw new IllegalArgumentException("expiresOn must not be before reservedOn");
        }
    }

    public static Reservation create(String id, String bookId, String memberId, LocalDate reservedOn) {
        Objects.requireNonNull(reservedOn, "reservedOn must not be null");
        return new Reservation(id, bookId, memberId, reservedOn, reservedOn.plusDays(EXPIRY_DAYS), ReservationStatus.ACTIVE);
    }

    public boolean isExpiredOn(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return status == ReservationStatus.ACTIVE && date.isAfter(expiresOn);
    }

    public Reservation expire() {
        return new Reservation(id, bookId, memberId, reservedOn, expiresOn, ReservationStatus.EXPIRED);
    }
}
