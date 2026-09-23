package com.eish.oms.order;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.eish.oms.common.Params;

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
                .param(Params.ORDER_ID, orderId)
                .param(Params.PRODUCT_ID, productId)
                .param(Params.WAREHOUSE_ID, warehouseId)
                .param(Params.QUANTITY, quantity)
                .param(Params.UNIT_PRICE, unitPrice)
                .update();
    }

    public List<OrderLineView> findViewsByOrder(long orderId) {
        return jdbc.sql(OrderLineSql.FIND_VIEWS_BY_ORDER)
                .param(Params.ORDER_ID, orderId)
                .query(OrderLineView.class)
                .list();
    }
}
