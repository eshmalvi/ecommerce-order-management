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

    /** Warehouses that currently hold at least the wanted quantity, fullest first. */
    static final String WAREHOUSES_WITH_STOCK = """
            select warehouse_id
              from inventory
             where product_id = :productId and quantity >= :quantity
             order by quantity desc, warehouse_id
            """;

    /**
     * The statement that prevents overselling. The availability check and the decrement are one statement,
     * so no other transaction can slip in between them. PostgreSQL re-evaluates the WHERE clause after
     * acquiring the row lock, so under the default READ COMMITTED isolation the update affects exactly
     * one row when there is enough stock and zero rows when there is not. Callers branch on that count.
     */
    static final String TRY_DECREMENT = """
            update inventory
               set quantity = quantity - :quantity
             where product_id = :productId
               and warehouse_id = :warehouseId
               and quantity >= :quantity
            """;

    /** Puts returned units back where they came from. */
    static final String RESTOCK = """
            update inventory
               set quantity = quantity + :quantity
             where product_id = :productId
               and warehouse_id = :warehouseId
            """;

    private InventorySql() {
    }
}
