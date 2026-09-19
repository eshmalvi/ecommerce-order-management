package com.eish.oms.order;

/**
 * SQL statements for the {@code order_line} table.
 */
final class OrderLineSql {

    static final String INSERT = """
            insert into order_line (order_id, product_id, warehouse_id, quantity, unit_price)
            values (:orderId, :productId, :warehouseId, :quantity, :unitPrice)
            """;

    /** Lines of one order with product and warehouse names, in the order they were added. */
    static final String FIND_VIEWS_BY_ORDER = """
            select l.product_id, p.sku, p.name as product_name,
                   l.warehouse_id, w.name as warehouse_name,
                   l.quantity, l.unit_price
              from order_line l
              join product p on p.id = l.product_id
              join warehouse w on w.id = l.warehouse_id
             where l.order_id = :orderId
             order by l.id
            """;

    private OrderLineSql() {
    }
}
