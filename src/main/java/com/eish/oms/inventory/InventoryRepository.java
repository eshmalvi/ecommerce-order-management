package com.eish.oms.inventory;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Data access for the {@code inventory} table. The SQL lives in {@link InventorySql}.
 */
@Repository
public class InventoryRepository {

    private final JdbcClient jdbc;

    public InventoryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Sets the stock of a product in a warehouse, creating the row if it does not exist yet.
     * One statement, so two admins saving at the same time cannot lose each other's write.
     */
    public Inventory upsert(long productId, long warehouseId, int quantity) {
        return jdbc.sql(InventorySql.UPSERT)
                .param("productId", productId)
                .param("warehouseId", warehouseId)
                .param("quantity", quantity)
                .query(Inventory.class)
                .single();
    }

    /** Ids of the warehouses that currently hold at least {@code quantity} of the product, fullest first. */
    public List<Long> findWarehousesWithStock(long productId, int quantity) {
        return jdbc.sql(InventorySql.WAREHOUSES_WITH_STOCK)
                .param("productId", productId)
                .param("quantity", quantity)
                .query(Long.class)
                .list();
    }

    /**
     * Atomically takes {@code quantity} units from one warehouse if, and only if, it has that many.
     * Check and decrement are a single UPDATE (see {@link InventorySql#TRY_DECREMENT}), so concurrent
     * buyers can never both succeed on the same last unit.
     *
     * @return true if the stock was taken; false if the warehouse did not have enough at that instant
     */
    public boolean tryDecrement(long productId, long warehouseId, int quantity) {
        int updatedRows = jdbc.sql(InventorySql.TRY_DECREMENT)
                .param("productId", productId)
                .param("warehouseId", warehouseId)
                .param("quantity", quantity)
                .update();
        return updatedRows == 1;
    }

    /** Adds {@code quantity} units back to the warehouse they were taken from. */
    public void restock(long productId, long warehouseId, int quantity) {
        jdbc.sql(InventorySql.RESTOCK)
                .param("productId", productId)
                .param("warehouseId", warehouseId)
                .param("quantity", quantity)
                .update();
    }

    public List<Inventory> findAll() {
        return jdbc.sql(InventorySql.FIND_ALL).query(Inventory.class).list();
    }

    public List<Inventory> findByProduct(long productId) {
        return jdbc.sql(InventorySql.FIND_BY_PRODUCT)
                .param("productId", productId)
                .query(Inventory.class)
                .list();
    }

    public Optional<Inventory> find(long productId, long warehouseId) {
        return jdbc.sql(InventorySql.FIND_BY_PRODUCT_AND_WAREHOUSE)
                .param("productId", productId)
                .param("warehouseId", warehouseId)
                .query(Inventory.class)
                .optional();
    }
}
