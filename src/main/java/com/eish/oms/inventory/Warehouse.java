package com.eish.oms.inventory;

/**
 * A location that holds stock, as stored in the {@code warehouse} table.
 */
public record Warehouse(Long id, String name) {
}
