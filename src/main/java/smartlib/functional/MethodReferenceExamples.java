package smartlib.functional;
// Quick note: this part reads better to me as a stream pipeline than a loop.

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import smartlib.domain.Book;
import smartlib.domain.Member;
import smartlib.domain.Notification;
import smartlib.domain.NotificationChannel;

/**
 * Small examples of the four method reference forms.
 */
public final class MethodReferenceExamples {

    private MethodReferenceExamples() {
    }

    public static List<String> normalisedTitles(List<Book> books) {
        return books.stream()
                .map(Book::title)
                .map(MethodReferenceExamples::normaliseTitle)
                .toList();
    }

    public static List<String> authorNames(List<Book> books) {
        return books.stream()
                .map(Book::author)
                .toList();
    }

    public static List<String> greetingsForMembers(List<Member> members, String prefix) {
        PrefixFormatter formatter = new PrefixFormatter(prefix);
        return members.stream()
                .map(Member::name)
                .map(formatter::format)
                .toList();
    }

    public static List<MemberNotification> notificationTargets(List<Member> members) {
        return members.stream()
                .map(MemberNotification::new)
                .toList();
    }

    public static Notification notificationFor(Member member, String subject, String message) {
        Objects.requireNonNull(member, "member must not be null");
        return new Notification(
                "notification-" + member.id(),
                member.id(),
                member.notificationAddress(),
                NotificationChannel.EMAIL,
                subject,
                message,
                LocalDateTime.now());
    }

    static String normaliseTitle(String title) {
        return title.trim().replaceAll("\\s+", " ");
    }

    private record PrefixFormatter(String prefix) {
        String format(String value) {
            return prefix + value;
        }
    }

    public record MemberNotification(String recipient, String memberName) {
        public MemberNotification(Member member) {
            this(member.notificationAddress(), member.name());
        }
    }
}
