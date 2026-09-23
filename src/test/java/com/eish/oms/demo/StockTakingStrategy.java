package com.eish.oms.demo;

/**
 * One way of taking a unit of stock from an inventory row. The four implementations in this package are
 * raced against each other by {@link ConcurrencyStrategyDemoTest} to show which ones oversell and what each
 * correct one costs. Only {@link AtomicConditionalUpdate} is what the application actually uses.
 */
interface StockTakingStrategy {

    /** Human-readable name for the printed comparison. */
    String name();

    /**
     * Tries to take {@code quantity} units from one inventory row, in its own transaction.
     *
     * @return true if the stock was taken, false if there was not enough
     */
    boolean tryTake(long inventoryId, int quantity);

    /** Database round trips one call makes on the happy path. */
    int roundTripsPerAttempt();

    /** How many times this strategy had to retry across the whole run (zero for most). */
    default int retries() {
        return 0;
    }
}
