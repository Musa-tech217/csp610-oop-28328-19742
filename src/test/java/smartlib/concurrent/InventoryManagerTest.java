package smartlib.concurrent;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class InventoryManagerTest {

    @Test
    void borrowWaitsUntilCopyIsReturned() throws Exception {
        InventoryManager inventoryManager = new InventoryManager(Map.of("ISBN-1", 0));
        CountDownLatch startedWaiting = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Future<Boolean> future = executorService.submit(() -> {
            startedWaiting.countDown();
            return inventoryManager.borrow("ISBN-1", 1_000);
        });

        assertTrue(startedWaiting.await(1, TimeUnit.SECONDS));
        Thread.sleep(100);
        inventoryManager.returnCopy("ISBN-1");

        assertTrue(future.get(2, TimeUnit.SECONDS));
        assertEquals(0, inventoryManager.availableCopies("ISBN-1"));
        executorService.shutdownNow();
    }

    @Test
    void borrowTimesOutWhenNoCopyArrives() throws Exception {
        InventoryManager inventoryManager = new InventoryManager(Map.of("ISBN-1", 0));

        boolean borrowed = inventoryManager.borrow("ISBN-1", 100);

        assertFalse(borrowed);
    }

    @Test
    void concurrentBorrowsAndReturnsDoNotLoseUpdates() throws Exception {
        InventoryManager inventoryManager = new InventoryManager(Map.of("ISBN-1", 10));
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        List<Future<?>> futures = java.util.stream.Stream.concat(
                java.util.stream.IntStream.range(0, 10)
                        .mapToObj(index -> executorService.submit(() -> {
                            try {
                                inventoryManager.borrow("ISBN-1", 500);
                            } catch (InterruptedException interruptedException) {
                                Thread.currentThread().interrupt();
                            }
                        })),
                java.util.stream.IntStream.range(0, 10)
                        .mapToObj(index -> executorService.submit(() -> inventoryManager.returnCopy("ISBN-1"))))
                .toList();

        for (Future<?> future : futures) {
            future.get(2, TimeUnit.SECONDS);
        }

        assertEquals(10, inventoryManager.availableCopies("ISBN-1"));
        executorService.shutdownNow();
    }

    @Test
    void availableCopiesSupportsConcurrentReaders() throws Exception {
        InventoryManager inventoryManager = new InventoryManager(Map.of("ISBN-1", 3));
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        List<Future<Integer>> reads = java.util.stream.IntStream.range(0, 8)
                .mapToObj(index -> executorService.submit(() -> inventoryManager.availableCopies("ISBN-1")))
                .toList();

        for (Future<Integer> future : reads) {
            assertEquals(3, future.get(1, TimeUnit.SECONDS));
        }

        executorService.shutdownNow();
    }
}
