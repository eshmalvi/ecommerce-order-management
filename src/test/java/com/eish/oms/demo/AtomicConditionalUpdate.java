package com.eish.oms.demo;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import com.eish.oms.common.Db;

/**
 * What the application does: check and decrement in one statement.
 *
 * <pre>
 * update inventory set quantity = quantity - :q where id = :id and quantity >= :q
 * </pre>
 *
 * <p>PostgreSQL takes the row lock, re-evaluates the WHERE against the current value, and either updates
 * one row or none. One round trip, no explicit lock, no retry loop: a loser learns immediately from the
 * row count. This is the same statement as {@code InventorySql.TRY_DECREMENT}, written here against the
 * row id so all four strategies share one harness.
 */
final class AtomicConditionalUpdate implements StockTakingStrategy {

    private final JdbcClient jdbc;
    private final TransactionTemplate tx;

    AtomicConditionalUpdate(JdbcClient jdbc, TransactionTemplate tx) {
        this.jdbc = jdbc;
        this.tx = tx;
    }

    @Override
    public String name() {
        return "atomic conditional UPDATE (production)";
    }

    @Override
    public boolean tryTake(long inventoryId, int quantity) {
        return Boolean.TRUE.equals(tx.execute(status -> {
            int updated = jdbc.sql("update %s set %s = %s - :quantity where %s = :id and %s >= :quantity"
                            .formatted(Db.Inventory.TABLE, Db.Inventory.QUANTITY, Db.Inventory.QUANTITY,
                                    Db.Inventory.ID, Db.Inventory.QUANTITY))
                    .param("quantity", quantity)
                    .param("id", inventoryId)
                    .update();
            return updated == 1;
        }));
    }

    @Override
    public int roundTripsPerAttempt() {
        return 1;
    }
}
