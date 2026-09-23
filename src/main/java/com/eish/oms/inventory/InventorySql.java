package com.eish.oms.inventory;

import static com.eish.oms.common.Db.Inventory.COLUMNS;
import static com.eish.oms.common.Db.Inventory.PRODUCT_ID;
import static com.eish.oms.common.Db.Inventory.QUANTITY;
import static com.eish.oms.common.Db.Inventory.TABLE;
import static com.eish.oms.common.Db.Inventory.WAREHOUSE_ID;
import static com.eish.oms.common.Params.bind;

import com.eish.oms.common.Params;

/**
 * SQL statements for the {@code inventory} table. Names come from {@code Db}; the grammar stays literal.
 */
final class InventorySql {

    /** Create or overwrite the stock level of one product in one warehouse, in a single statement. */
    static final String UPSERT = """
            insert into %s (%s, %s, %s)
            values (%s, %s, %s)
            on conflict (%s, %s) do update set %s = excluded.%s
            returning %s
            """.formatted(TABLE, PRODUCT_ID, WAREHOUSE_ID, QUANTITY,
                    bind(Params.PRODUCT_ID), bind(Params.WAREHOUSE_ID), bind(Params.QUANTITY),
                    PRODUCT_ID, WAREHOUSE_ID, QUANTITY, QUANTITY,
                    COLUMNS);

    static final String FIND_ALL = "select %s from %s order by %s, %s"
            .formatted(COLUMNS, TABLE, PRODUCT_ID, WAREHOUSE_ID);

    static final String FIND_BY_PRODUCT = "select %s from %s where %s = %s order by %s"
            .formatted(COLUMNS, TABLE, PRODUCT_ID, bind(Params.PRODUCT_ID), WAREHOUSE_ID);

    static final String FIND_BY_PRODUCT_AND_WAREHOUSE = "select %s from %s where %s = %s and %s = %s"
            .formatted(COLUMNS, TABLE, PRODUCT_ID, bind(Params.PRODUCT_ID), WAREHOUSE_ID, bind(Params.WAREHOUSE_ID));

    /** Warehouses that currently hold at least the wanted quantity, fullest first. */
    static final String WAREHOUSES_WITH_STOCK = """
            select %s
              from %s
             where %s = %s and %s >= %s
             order by %s desc, %s
            """.formatted(WAREHOUSE_ID,
                    TABLE,
                    PRODUCT_ID, bind(Params.PRODUCT_ID), QUANTITY, bind(Params.QUANTITY),
                    QUANTITY, WAREHOUSE_ID);

    /**
     * The statement that prevents overselling. The availability check and the decrement are one statement,
     * so no other transaction can slip in between them. PostgreSQL re-evaluates the WHERE clause after
     * acquiring the row lock, so under the default READ COMMITTED isolation the update affects exactly
     * one row when there is enough stock and zero rows when there is not. Callers branch on that count.
     *
     * <pre>
     * update inventory set quantity = quantity - :quantity
     *  where product_id = :productId and warehouse_id = :warehouseId and quantity >= :quantity
     * </pre>
     */
    static final String TRY_DECREMENT = """
            update %s
               set %s = %s - %s
             where %s = %s
               and %s = %s
               and %s >= %s
            """.formatted(TABLE,
                    QUANTITY, QUANTITY, bind(Params.QUANTITY),
                    PRODUCT_ID, bind(Params.PRODUCT_ID),
                    WAREHOUSE_ID, bind(Params.WAREHOUSE_ID),
                    QUANTITY, bind(Params.QUANTITY));

    /** Puts returned units back where they came from. */
    static final String RESTOCK = """
            update %s
               set %s = %s + %s
             where %s = %s
               and %s = %s
            """.formatted(TABLE,
                    QUANTITY, QUANTITY, bind(Params.QUANTITY),
                    PRODUCT_ID, bind(Params.PRODUCT_ID),
                    WAREHOUSE_ID, bind(Params.WAREHOUSE_ID));

    private InventorySql() {
    }
}
