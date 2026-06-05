package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import smartlib.modern.MembershipPolicyFactory;

/**
 * Resolves the correct policy without hard-coding membership conditionals in services.
 */
public final class BorrowingPolicyRegistry {

    private final Map<MembershipType, BorrowingPolicy> policies;

    public BorrowingPolicyRegistry(Collection<BorrowingPolicy> policies) {
        Objects.requireNonNull(policies, "policies must not be null");
        this.policies = policies.stream()
                .collect(Collectors.toUnmodifiableMap(BorrowingPolicy::membershipType, Function.identity()));
    }

    public BorrowingPolicy getPolicyFor(MembershipType membershipType) {
        Objects.requireNonNull(membershipType, "membershipType must not be null");
        BorrowingPolicy policy = policies.getOrDefault(
                membershipType,
                MembershipPolicyFactory.defaultFactory().create(membershipType));
        if (policy == null) {
            throw new IllegalArgumentException("No borrowing policy configured for " + membershipType);
        }
        return policy;
    }
}
