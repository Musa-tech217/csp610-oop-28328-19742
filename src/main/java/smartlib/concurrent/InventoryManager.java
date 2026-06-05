package smartlib.concurrent;
// Quick note: I kept the shared state small here because that felt safer.

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe inventory manager using read/write locks and a condition for waiting borrowers.
 */
public final class InventoryManager {

    private final Map<String, Integer> inventory;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Condition copiesAvailable = lock.writeLock().newCondition();

    public InventoryManager(Map<String, Integer> initialInventory) {
        Objects.requireNonNull(initialInventory, "initialInventory must not be null");
        this.inventory = new HashMap<>(initialInventory);
    }

    public boolean borrow(String isbn, long timeoutMs) throws InterruptedException {
        Objects.requireNonNull(isbn, "isbn must not be null");
        if (timeoutMs < 0) {
            throw new IllegalArgumentException("timeoutMs must not be negative");
        }

        long nanosRemaining = TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        lock.writeLock().lockInterruptibly();
        try {
            while (inventory.getOrDefault(isbn, 0) <= 0) {
                if (nanosRemaining <= 0L) {
                    return false;
                }
                nanosRemaining = copiesAvailable.awaitNanos(nanosRemaining);
            }
            inventory.put(isbn, inventory.get(isbn) - 1);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void returnCopy(String isbn) {
        Objects.requireNonNull(isbn, "isbn must not be null");
        lock.writeLock().lock();
        try {
            inventory.merge(isbn, 1, Integer::sum);
            copiesAvailable.signalAll();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int availableCopies(String isbn) {
        Objects.requireNonNull(isbn, "isbn must not be null");
        lock.readLock().lock();
        try {
            return inventory.getOrDefault(isbn, 0);
        } finally {
            lock.readLock().unlock();
        }
    }
}
