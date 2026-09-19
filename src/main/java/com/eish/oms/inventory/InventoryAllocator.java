package com.eish.oms.inventory;

import org.springframework.stereotype.Service;

import com.eish.oms.common.InsufficientStockException;

/**
 * Decides which warehouse an order line is fulfilled from, and takes the stock from it.
 *
 * <p>Strategy: try the warehouses that have enough stock, fullest first, and keep the first one whose
 * atomic decrement succeeds. Losing a race against another buyer simply means moving on to the next
 * candidate. Each line comes from a single warehouse; splitting one line across warehouses is a
 * documented non-goal.
 *
 * <p>Runs inside the caller's transaction when there is one (checkout), so a later failure in the same
 * transaction rolls the decrement back. Standalone, each statement commits on its own.
 */
@Service
public class InventoryAllocator {

    private final InventoryRepository inventory;

    public InventoryAllocator(InventoryRepository inventory) {
        this.inventory = inventory;
    }

    /**
     * Takes {@code quantity} units of a product from one warehouse and returns that warehouse's id.
     *
     * @throws InsufficientStockException when no single warehouse can supply the quantity
     */
    public long allocate(long productId, int quantity) {
        for (long warehouseId : inventory.findWarehousesWithStock(productId, quantity)) {
            if (inventory.tryDecrement(productId, warehouseId, quantity)) {
                return warehouseId;
            }
        }
        throw new InsufficientStockException(productId, quantity);
    }
}
