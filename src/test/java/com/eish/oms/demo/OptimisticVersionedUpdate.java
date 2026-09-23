package com.eish.oms.demo;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import com.eish.oms.common.Db;

/**
 * Correct, the optimistic way: read the row, then update it only if it is still the row you read
 * ({@code WHERE quantity = :expected} acts as the version check). If another buyer changed it first the
 * update touches zero rows, and you read again and retry.
 *
 * <p>No lock is held between the read and the write, which is attractive when contention is rare. Under
 * fifty buyers on one row it is the wrong tool: almost every attempt loses the race at least once, and the
 * retries pile up. The counter makes that visible.
 */
final class OptimisticVersionedUpdate implements StockTakingStrategy {

    private static final int MAX_ATTEMPTS = 100;

    private final JdbcClient jdbc;
    private final TransactionTemplate tx;
    private final AtomicInteger retries = new AtomicInteger();

    OptimisticVersionedUpdate(JdbcClient jdbc, TransactionTemplate tx) {
        this.jdbc = jdbc;
        this.tx = tx;
    }

    @Override
    public String name() {
        return "optimistic compare-and-retry";
    }

    @Override
    public boolean tryTake(long inventoryId, int quantity) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            Boolean result = tx.execute(status -> {
                int expected = jdbc.sql("select %s from %s where %s = :id"
                                .formatted(Db.Inventory.QUANTITY, Db.Inventory.TABLE, Db.Inventory.ID))
                        .param("id", inventoryId)
                        .query(Integer.class)
                        .single();
                if (expected < quantity) {
                    return false;
                }
                int updated = jdbc.sql("update %s set %s = :newQuantity where %s = :id and %s = :expected"
                                .formatted(Db.Inventory.TABLE, Db.Inventory.QUANTITY, Db.Inventory.ID, Db.Inventory.QUANTITY))
                        .param("newQuantity", expected - quantity)
                        .param("id", inventoryId)
                        .param("expected", expected)   // the version check: the row must be unchanged
                        .update();
                return updated == 1 ? Boolean.TRUE : null;   // null = lost the race, try again
            });
            if (result != null) {
                return result;
            }
            retries.incrementAndGet();
        }
        throw new IllegalStateException("gave up after " + MAX_ATTEMPTS + " attempts");
    }

    @Override
    public int roundTripsPerAttempt() {
        return 2;
    }

    @Override
    public int retries() {
        return retries.get();
    }
}
