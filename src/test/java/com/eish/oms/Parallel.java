package com.eish.oms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

/**
 * Runs many tasks at the same instant. Every task is started on its own thread, all threads wait on one
 * latch, and the latch is released once all of them are ready, so the tasks really race each other
 * instead of trickling in as the pool spins up.
 */
public final class Parallel {

    private Parallel() {
    }

    /**
     * Runs {@code count} tasks concurrently and returns their results in submission order.
     * A task that throws makes this method throw, which fails the test: only expected outcomes should be
     * returned as values by the task itself.
     */
    public static <T> List<T> run(int count, IntFunction<Callable<T>> taskFor) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(count);
        CountDownLatch ready = new CountDownLatch(count);
        CountDownLatch go = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Callable<T> task = taskFor.apply(i);
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();
                    return task.call();
                }));
            }
            ready.await(30, TimeUnit.SECONDS);
            go.countDown();

            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(60, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }
}
