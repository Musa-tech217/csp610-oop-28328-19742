package smartlib.concurrent;
// Quick note: I kept the shared state small here because that felt safer.

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Minimal SmartLib-themed examples of common concurrency hazards and concurrent collection choices.
 */
public final class ConcurrencyExamples {

    private ConcurrencyExamples() {
    }

    public static RaceConditionExample raceConditionExample() {
        return new RaceConditionExample(
                "Two staff terminals update the same borrow counter without coordination.",
                "The read-modify-write sequence is not atomic, so one increment can overwrite another.",
                "Use AtomicInteger, locks, or higher-level concurrent structures.");
    }

    public static DeadlockExample deadlockExample() {
        return new DeadlockExample(
                "Thread A locks the loan ledger then waits for the inventory lock while Thread B does the reverse.",
                "Each thread holds one lock and waits forever for the other.",
                "Keep a consistent lock ordering or reduce multi-lock designs.");
    }

    public static LivelockExample livelockExample() {
        return new LivelockExample(
                "Two assistants keep yielding the last copy to be polite and neither finishes the borrow flow.",
                "The threads are active but keep reacting to each other instead of making progress.",
                "Use backoff, bounded retries, or clearer ownership rules.");
    }

    public static Map<String, Integer> concurrentHashMapExample() {
        ConcurrentHashMap<String, AtomicInteger> borrowCounts = new ConcurrentHashMap<>();
        borrowCounts.computeIfAbsent("ISBN-1", ignored -> new AtomicInteger()).incrementAndGet();
        borrowCounts.computeIfAbsent("ISBN-1", ignored -> new AtomicInteger()).incrementAndGet();
        return Map.of("ISBN-1", borrowCounts.get("ISBN-1").get());
    }

    public static Map<String, Integer> synchronizedMapExample() {
        Map<String, Integer> guardedInventory = Collections.synchronizedMap(new HashMap<>());
        guardedInventory.put("ISBN-1", 1);
        synchronized (guardedInventory) {
            guardedInventory.put("ISBN-1", guardedInventory.get("ISBN-1") + 1);
            return Map.copyOf(guardedInventory);
        }
    }

    public record RaceConditionExample(String scenario, String whyItHappens, String mitigation) {
    }

    public record DeadlockExample(String scenario, String whyItHappens, String mitigation) {
    }

    public record LivelockExample(String scenario, String whyItHappens, String mitigation) {
    }
}
