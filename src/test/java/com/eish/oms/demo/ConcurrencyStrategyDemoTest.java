package com.eish.oms.demo;

import static com.eish.oms.SeedData.HEADPHONES_EAST;
import static com.eish.oms.SeedData.SKU_HEADPHONES;
import static com.eish.oms.SeedData.WAREHOUSE_EAST;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import com.eish.oms.AbstractIntegrationTest;
import com.eish.oms.Parallel;
import com.eish.oms.common.Db;

/**
 * Four ways of taking stock, raced under the same conditions: fifty buyers, one unit each, one row holding
 * six. A correct strategy lets exactly six through. The naive one does not, which is why it is tagged
 * {@code demo} and excluded from the normal build: it is here to be watched, not to pass.
 *
 * <p>Run with {@code ./mvnw test -Pdemo}. The printed table is the point.
 */
@Tag("demo")
class ConcurrencyStrategyDemoTest extends AbstractIntegrationTest {

    private static final int BUYERS = 50;

    @Autowired
    private TransactionTemplate tx;

    @Test
    void naiveReadCheckWriteOversells() throws Exception {
        Outcome outcome = race(new NaiveReadCheckWrite(jdbc, tx));
        print(outcome);

        assertThat(outcome.winners)
                .as("more buyers succeeded than there were units: the lost-update bug")
                .isGreaterThan(HEADPHONES_EAST);
        assertThat(outcome.stockAfter)
                .as("stock is not negative, so the check constraint is satisfied: it cannot catch this bug")
                .isGreaterThanOrEqualTo(0);
        assertThat(outcome.winners + outcome.stockAfter)
                .as("units sold plus units left exceeds units that existed")
                .isGreaterThan(HEADPHONES_EAST);
    }

    @Test
    void pessimisticLockingIsCorrectButPaysTwoRoundTripsUnderALock() throws Exception {
        Outcome outcome = race(new PessimisticSelectForUpdate(jdbc, tx));
        print(outcome);
        assertExactlySixSold(outcome);
    }

    @Test
    void optimisticLockingIsCorrectButRetryStormsOnAHotRow() throws Exception {
        OptimisticVersionedUpdate strategy = new OptimisticVersionedUpdate(jdbc, tx);
        Outcome outcome = race(strategy);
        print(outcome);
        assertExactlySixSold(outcome);
        assertThat(strategy.retries())
                .as("fifty buyers on one row collide repeatedly")
                .isGreaterThan(0);
    }

    @Test
    void atomicConditionalUpdateIsCorrectInOneRoundTripWithNoRetries() throws Exception {
        Outcome outcome = race(new AtomicConditionalUpdate(jdbc, tx));
        print(outcome);
        assertExactlySixSold(outcome);
        assertThat(outcome.retries).isZero();
        assertThat(outcome.roundTrips).isEqualTo(1);
    }

    private Outcome race(StockTakingStrategy strategy) throws Exception {
        long rowId = inventoryRowId(SKU_HEADPHONES, WAREHOUSE_EAST);   // holds 6 in the seed
        long started = System.nanoTime();
        List<Boolean> results = Parallel.run(BUYERS, buyer -> () -> strategy.tryTake(rowId, 1));
        long elapsedMs = (System.nanoTime() - started) / 1_000_000;
        int winners = (int) results.stream().filter(Boolean::booleanValue).count();
        return new Outcome(strategy.name(), winners, stock(SKU_HEADPHONES, WAREHOUSE_EAST),
                strategy.roundTripsPerAttempt(), strategy.retries(), elapsedMs);
    }

    private static void assertExactlySixSold(Outcome outcome) {
        assertThat(outcome.winners).isEqualTo(HEADPHONES_EAST);
        assertThat(outcome.stockAfter).isZero();
    }

    private long inventoryRowId(String sku, String warehouse) {
        return jdbc.sql("""
                select i.%s from %s i
                join %s p on p.%s = i.%s
                join %s w on w.%s = i.%s
                where p.%s = :sku and w.%s = :warehouse
                """.formatted(Db.Inventory.ID, Db.Inventory.TABLE,
                        Db.Product.TABLE, Db.Product.ID, Db.Inventory.PRODUCT_ID,
                        Db.Warehouse.TABLE, Db.Warehouse.ID, Db.Inventory.WAREHOUSE_ID,
                        Db.Product.SKU, Db.Warehouse.NAME))
                .param("sku", sku)
                .param("warehouse", warehouse)
                .query(Long.class)
                .single();
    }

    private static void print(Outcome o) {
        String verdict = o.winners == HEADPHONES_EAST ? "correct" : "OVERSOLD by " + (o.winners - HEADPHONES_EAST);
        System.out.printf("%n  %-42s buyers=%d units=%d sold=%d left=%d  round-trips=%d retries=%d  %dms  -> %s%n%n",
                o.strategy, BUYERS, HEADPHONES_EAST, o.winners, o.stockAfter, o.roundTrips, o.retries, o.elapsedMs, verdict);
    }

    private record Outcome(String strategy, int winners, int stockAfter, int roundTrips, int retries, long elapsedMs) {
    }
}
