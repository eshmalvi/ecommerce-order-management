package com.eish.oms.inventory;

/**
 * Stock of one product in one warehouse, as stored in the {@code inventory} table.
 * {@code quantity} is what is available to sell; the database guarantees it never goes negative.
 */
public record Inventory(Long id, Long productId, Long warehouseId, int quantity) {
}
