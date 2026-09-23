package com.eish.oms.demo;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import com.eish.oms.common.Db;

/**
 * Correct, the classic way: {@code SELECT ... FOR UPDATE} locks the row first, then the check and the write
 * happen under that lock, so nobody else can read a stale value in between.
 *
 * <p>The cost is two round trips per attempt and a lock that is held across Java code, not just for a single
 * statement. Losers still block and wait for the winner's transaction to finish before they learn there is
 * nothing left.
 */
final class PessimisticSelectForUpdate implements StockTakingStrategy {

    private final JdbcClient jdbc;
    private final TransactionTemplate tx;

    PessimisticSelectForUpdate(JdbcClient jdbc, TransactionTemplate tx) {
        this.jdbc = jdbc;
        this.tx = tx;
    }

    @Override
    public String name() {
        return "pessimistic SELECT FOR UPDATE";
    }

    @Override
    public boolean tryTake(long inventoryId, int quantity) {
        return Boolean.TRUE.equals(tx.execute(status -> {
            int current = jdbc.sql("select %s from %s where %s = :id for update"
                            .formatted(Db.Inventory.QUANTITY, Db.Inventory.TABLE, Db.Inventory.ID))
                    .param("id", inventoryId)
                    .query(Integer.class)
                    .single();
            if (current < quantity) {
                return false;
            }
            jdbc.sql("update %s set %s = :newQuantity where %s = :id"
                            .formatted(Db.Inventory.TABLE, Db.Inventory.QUANTITY, Db.Inventory.ID))
                    .param("newQuantity", current - quantity)   // safe here only because the row is locked
                    .param("id", inventoryId)
                    .update();
            return true;
        }));
    }

    @Override
    public int roundTripsPerAttempt() {
        return 2;
    }
}
