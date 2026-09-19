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
