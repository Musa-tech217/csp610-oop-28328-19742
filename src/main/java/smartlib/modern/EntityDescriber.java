package smartlib.modern;
// Quick note: I used the newer Java feature here because it made the intent clearer.

import smartlib.domain.Book;
import smartlib.domain.Fine;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.Notification;
import smartlib.domain.Reservation;

/**
 * Describes domain entities using pattern matching for instanceof.
 */
public final class EntityDescriber {

    public String describe(Object entity) {
        if (entity instanceof Book book) {
            return "Book[%s by %s, isbn=%s]".formatted(book.title(), book.author(), book.isbn());
        }
        if (entity instanceof Member member) {
            return "Member[%s, membership=%s]".formatted(member.name(), member.membershipType());
        }
        if (entity instanceof Loan loan) {
            return "Loan[%s -> %s, due=%s]".formatted(loan.memberId(), loan.bookId(), loan.dueOn());
        }
        if (entity instanceof Reservation reservation) {
            return "Reservation[%s, expires=%s]".formatted(reservation.bookId(), reservation.expiresOn());
        }
        if (entity instanceof Fine fine) {
            return "Fine[$%s for loan %s]".formatted(fine.amount(), fine.loanId());
        }
        if (entity instanceof Notification notification) {
            return "Notification[to=%s via %s]".formatted(notification.recipient(), notification.channel());
        }
        return "Unknown entity";
    }
}
