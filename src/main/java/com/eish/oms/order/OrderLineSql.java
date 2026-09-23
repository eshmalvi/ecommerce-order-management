package com.eish.oms.order;

import static com.eish.oms.common.Db.OrderLine.ID;
import static com.eish.oms.common.Db.OrderLine.ORDER_ID;
import static com.eish.oms.common.Db.OrderLine.PRODUCT_ID;
import static com.eish.oms.common.Db.OrderLine.QUANTITY;
import static com.eish.oms.common.Db.OrderLine.TABLE;
import static com.eish.oms.common.Db.OrderLine.UNIT_PRICE;
import static com.eish.oms.common.Db.OrderLine.WAREHOUSE_ID;
import static com.eish.oms.common.Params.bind;

import com.eish.oms.common.Db;
import com.eish.oms.common.Params;

/**
 * SQL statements for the {@code order_line} table. Names come from {@code Db}; the grammar stays literal.
 */
final class OrderLineSql {

    static final String INSERT = """
            insert into %s (%s, %s, %s, %s, %s)
            values (%s, %s, %s, %s, %s)
            """.formatted(TABLE, ORDER_ID, PRODUCT_ID, WAREHOUSE_ID, QUANTITY, UNIT_PRICE,
                    bind(Params.ORDER_ID), bind(Params.PRODUCT_ID), bind(Params.WAREHOUSE_ID),
                    bind(Params.QUANTITY), bind(Params.UNIT_PRICE));

    /**
     * Lines of one order with product and warehouse names, in the order they were added.
     * Column aliases match the {@link OrderLineView} record components.
     */
    static final String FIND_VIEWS_BY_ORDER = """
            select l.%s as product_id, p.%s as sku, p.%s as product_name,
                   l.%s as warehouse_id, w.%s as warehouse_name,
                   l.%s as quantity, l.%s as unit_price
              from %s l
              join %s p on p.%s = l.%s
              join %s w on w.%s = l.%s
             where l.%s = %s
             order by l.%s
            """.formatted(PRODUCT_ID, Db.Product.SKU, Db.Product.NAME,
                    WAREHOUSE_ID, Db.Warehouse.NAME,
                    QUANTITY, UNIT_PRICE,
                    TABLE,
                    Db.Product.TABLE, Db.Product.ID, PRODUCT_ID,
                    Db.Warehouse.TABLE, Db.Warehouse.ID, WAREHOUSE_ID,
                    ORDER_ID, bind(Params.ORDER_ID),
                    ID);

    private OrderLineSql() {
    }
}
