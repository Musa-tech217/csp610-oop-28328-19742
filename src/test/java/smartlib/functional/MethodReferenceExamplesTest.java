package smartlib.functional;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import smartlib.domain.Book;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.NotificationChannel;

class MethodReferenceExamplesTest {

    @Test
    void staticMethodReferenceNormalisesTitles() {
        List<Book> books = List.of(
                new Book("book-1", " Clean   Code ", "Robert Martin", "ISBN-1", "Programming", 2008, 1));

        assertEquals(List.of("Clean Code"), MethodReferenceExamples.normalisedTitles(books));
    }

    @Test
    void unboundInstanceMethodReferenceReadsAuthors() {
        List<Book> books = List.of(
                new Book("book-1", "Clean Code", "Robert Martin", "ISBN-1", "Programming", 2008, 1),
                new Book("book-2", "Effective Java", "Joshua Bloch", "ISBN-2", "Programming", 2018, 1));

        assertEquals(List.of("Robert Martin", "Joshua Bloch"), MethodReferenceExamples.authorNames(books));
    }

    @Test
    void boundInstanceMethodReferenceUsesSpecificFormatterObject() {
        List<Member> members = List.of(member("member-1", "Alice"), member("member-2", "Bob"));

        assertEquals(List.of("Hello Alice", "Hello Bob"),
                MethodReferenceExamples.greetingsForMembers(members, "Hello "));
    }

    @Test
    void constructorReferenceBuildsNotificationTargets() {
        List<Member> members = List.of(member("member-1", "Alice"));

        List<MethodReferenceExamples.MemberNotification> targets = MethodReferenceExamples.notificationTargets(members);

        assertEquals("member-1@example.com", targets.get(0).recipient());
        assertEquals("Alice", targets.get(0).memberName());
    }

    private Member member(String id, String name) {
        return new Member(id, name, id + "@example.com", MembershipType.STANDARD, BigDecimal.ZERO, NotificationChannel.EMAIL);
    }
}
