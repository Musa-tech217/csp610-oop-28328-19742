package smartlib.modern;
// Quick note: I used the newer Java feature here because it made the intent clearer.

import java.util.Objects;
import smartlib.domain.BorrowingPolicy;
import smartlib.domain.MembershipType;
import smartlib.domain.PremiumPolicy;
import smartlib.domain.StandardPolicy;
import smartlib.domain.StudentPolicy;

/**
 * Switch-expression based factory for membership borrowing policies.
 */
public final class MembershipPolicyFactory {

    private static final MembershipPolicyFactory DEFAULT = new MembershipPolicyFactory();

    public static MembershipPolicyFactory defaultFactory() {
        return DEFAULT;
    }

    public BorrowingPolicy create(MembershipType membershipType) {
        Objects.requireNonNull(membershipType, "membershipType must not be null");
        return switch (membershipType) {
            case STANDARD -> new StandardPolicy();
            case PREMIUM -> new PremiumPolicy();
            case STUDENT -> new StudentPolicy();
        };
    }
}
