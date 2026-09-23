package com.eish.oms.demo;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import com.eish.oms.common.Db;

/**
 * The bug. Read the quantity, check it in Java, write the new value back.
 *
 * <p>Two buyers can both read 6, both pass the check, and both write 5. Two units are sold and stock drops
 * by one. Under fifty threads this oversells badly, and the database's {@code check (quantity >= 0)}
 * cannot help, because 5 is a perfectly valid value to write. The {@code Thread.sleep} only widens the
 * window so the race shows every time; real code has exactly the same window, just narrower.
 */
final class NaiveReadCheckWrite implements StockTakingStrategy {

    private final JdbcClient jdbc;
    private final TransactionTemplate tx;

    NaiveReadCheckWrite(JdbcClient jdbc, TransactionTemplate tx) {
        this.jdbc = jdbc;
        this.tx = tx;
    }

    @Override
    public String name() {
        return "naive read-check-write";
    }

    @Override
    public boolean tryTake(long inventoryId, int quantity) {
        return Boolean.TRUE.equals(tx.execute(status -> {
            int current = jdbc.sql("select %s from %s where %s = :id"
                            .formatted(Db.Inventory.QUANTITY, Db.Inventory.TABLE, Db.Inventory.ID))
                    .param("id", inventoryId)
                    .query(Integer.class)
                    .single();
            if (current < quantity) {
                return false;
            }
            sleepToWidenTheRaceWindow();
            jdbc.sql("update %s set %s = :newQuantity where %s = :id"
                            .formatted(Db.Inventory.TABLE, Db.Inventory.QUANTITY, Db.Inventory.ID))
                    .param("newQuantity", current - quantity)   // a value computed in Java: the lost update
                    .param("id", inventoryId)
                    .update();
            return true;
        }));
    }

    @Override
    public int roundTripsPerAttempt() {
        return 2;
    }

    private static void sleepToWidenTheRaceWindow() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
