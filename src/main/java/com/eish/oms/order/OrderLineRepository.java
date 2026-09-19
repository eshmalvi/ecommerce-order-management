package com.eish.oms.order;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Data access for the {@code order_line} table. The SQL lives in {@link OrderLineSql}.
 */
@Repository
public class OrderLineRepository {

    private final JdbcClient jdbc;

    public OrderLineRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Adds one line to an order, recording the warehouse it ships from and the price paid. */
    public void insert(long orderId, long productId, long warehouseId, int quantity, BigDecimal unitPrice) {
        jdbc.sql(OrderLineSql.INSERT)
                .param("orderId", orderId)
                .param("productId", productId)
                .param("warehouseId", warehouseId)
                .param("quantity", quantity)
                .param("unitPrice", unitPrice)
                .update();
    }

    public List<OrderLineView> findViewsByOrder(long orderId) {
        return jdbc.sql(OrderLineSql.FIND_VIEWS_BY_ORDER)
                .param("orderId", orderId)
                .query(OrderLineView.class)
                .list();
    }
}
