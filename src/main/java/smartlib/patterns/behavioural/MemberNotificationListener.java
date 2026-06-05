package smartlib.patterns.behavioural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import smartlib.domain.Member;
import smartlib.domain.Notification;
import smartlib.domain.NotificationChannel;
import smartlib.domain.NotificationSender;
import smartlib.domain.Repository;
import smartlib.domain.Reservation;
import smartlib.domain.Fine;

/**
 * Sends member-facing notifications in response to selected library events.
 */
public final class MemberNotificationListener implements LibraryEventListener {

    private final Repository<Notification, String> notificationRepository;
    private final NotificationSender notificationSender;

    public MemberNotificationListener(
            Repository<Notification, String> notificationRepository,
            NotificationSender notificationSender) {
        this.notificationRepository = Objects.requireNonNull(notificationRepository, "notificationRepository must not be null");
        this.notificationSender = Objects.requireNonNull(notificationSender, "notificationSender must not be null");
    }

    @Override
    public void onEvent(LibraryEvent event) {
        Notification notification = switch (event.type()) {
            case RESERVATION_EXPIRED -> buildReservationExpiredNotification(event);
            case FINE_IMPOSED -> buildFineImposedNotification(event);
            default -> null;
        };
        if (notification == null) {
            return;
        }
        notificationRepository.save(notification);
        notificationSender.send(notification);
    }

    private Notification buildReservationExpiredNotification(LibraryEvent event) {
        Reservation reservation = event.payload("reservation", Reservation.class);
        Member member = event.payload("member", Member.class);
        return new Notification(
                UUID.randomUUID().toString(),
                member.id(),
                member.notificationAddress(),
                preferredChannel(member),
                "Reservation expired",
                "Your reservation " + reservation.id() + " for book " + reservation.bookId() + " has expired.",
                LocalDateTime.now());
    }

    private Notification buildFineImposedNotification(LibraryEvent event) {
        Fine fine = event.payload("fine", Fine.class);
        Member member = event.payload("member", Member.class);
        return new Notification(
                UUID.randomUUID().toString(),
                member.id(),
                member.notificationAddress(),
                preferredChannel(member),
                "Fine imposed",
                "A fine of $" + fine.amount() + " has been imposed on your account.",
                LocalDateTime.now());
    }

    private NotificationChannel preferredChannel(Member member) {
        return member.preferredChannel();
    }
}
