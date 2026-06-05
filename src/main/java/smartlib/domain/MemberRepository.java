package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

public final class MemberRepository extends InMemoryRepository<Member, String> {

    public MemberRepository() {
        super(Member::id);
    }
}
