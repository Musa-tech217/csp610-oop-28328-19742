package smartlib.modern;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import smartlib.domain.MembershipType;
import smartlib.domain.PremiumPolicy;
import smartlib.domain.StandardPolicy;
import smartlib.domain.StudentPolicy;

class MembershipPolicyFactoryTest {

    @Test
    void switchExpressionReturnsCorrectPolicy() {
        MembershipPolicyFactory factory = MembershipPolicyFactory.defaultFactory();

        assertInstanceOf(StandardPolicy.class, factory.create(MembershipType.STANDARD));
        assertInstanceOf(PremiumPolicy.class, factory.create(MembershipType.PREMIUM));
        assertInstanceOf(StudentPolicy.class, factory.create(MembershipType.STUDENT));
        assertEquals(14, factory.create(MembershipType.STANDARD).loanPeriodDays());
    }
}
