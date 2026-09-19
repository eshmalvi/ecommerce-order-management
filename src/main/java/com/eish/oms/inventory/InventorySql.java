package com.eish.oms.inventory;

/**
 * SQL statements for the {@code inventory} table.
 */
final class InventorySql {

    private static final String COLUMNS = "id, product_id, warehouse_id, quantity";

    /** Create or overwrite the stock level of one product in one warehouse, in a single statement. */
    static final String UPSERT = """
            insert into inventory (product_id, warehouse_id, quantity)
            values (:productId, :warehouseId, :quantity)
            on conflict (product_id, warehouse_id) do update set quantity = excluded.quantity
            returning id, product_id, warehouse_id, quantity
            """;

    static final String FIND_ALL =
            "select " + COLUMNS + " from inventory order by product_id, warehouse_id";

    static final String FIND_BY_PRODUCT =
            "select " + COLUMNS + " from inventory where product_id = :productId order by warehouse_id";

    static final String FIND_BY_PRODUCT_AND_WAREHOUSE =
            "select " + COLUMNS + " from inventory where product_id = :productId and warehouse_id = :warehouseId";

    private InventorySql() {
    }
}
