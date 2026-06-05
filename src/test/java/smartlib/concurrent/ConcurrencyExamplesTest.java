package smartlib.concurrent;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ConcurrencyExamplesTest {

    @Test
    void concurrentCollectionExamplesProduceExpectedSnapshots() {
        assertEquals(Map.of("ISBN-1", 2), ConcurrencyExamples.concurrentHashMapExample());
        assertEquals(Map.of("ISBN-1", 2), ConcurrencyExamples.synchronizedMapExample());
    }

    @Test
    void hazardExamplesDescribeTheProblemAndMitigation() {
        assertTrue(ConcurrencyExamples.raceConditionExample().whyItHappens().contains("not atomic"));
        assertTrue(ConcurrencyExamples.deadlockExample().mitigation().contains("lock ordering"));
        assertTrue(ConcurrencyExamples.livelockExample().scenario().contains("assistants"));
    }
}
