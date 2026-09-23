package com.eish.oms.order;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.eish.oms.common.Params;
import com.eish.oms.pricing.PriceBreakdown;

/**
 * Data access for the {@code orders} table. The SQL lives in {@link OrderSql}.
 */
@Repository
public class OrderRepository {

    private final JdbcClient jdbc;

    public OrderRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Creates the order in its first status and returns the new id. */
    public long insert(String customer, OrderStatus status, PriceBreakdown price, String discountCode) {
        return jdbc.sql(OrderSql.INSERT)
                .param(Params.CUSTOMER, customer)
                .param(Params.STATUS, status.name())
                .param(Params.SUBTOTAL, price.subtotal())
                .param(Params.DISCOUNT_AMOUNT, price.discountAmount())
                .param(Params.TAX_AMOUNT, price.taxAmount())
                .param(Params.TOTAL, price.total())
                .param(Params.DISCOUNT_CODE, discountCode)
                .query(Long.class)
                .single();
    }

    /** Records a successful payment and moves the order to {@code status}. */
    public void confirm(long orderId, OrderStatus status, String paymentRef) {
        jdbc.sql(OrderSql.CONFIRM)
                .param(Params.ID, orderId)
                .param(Params.STATUS, status.name())
                .param(Params.PAYMENT_REF, paymentRef)
                .update();
    }

    /** Records a refund and moves the order to {@code status}. */
    public void markReturned(long orderId, OrderStatus status, String refundRef) {
        jdbc.sql(OrderSql.MARK_RETURNED)
                .param(Params.ID, orderId)
                .param(Params.STATUS, status.name())
                .param(Params.REFUND_REF, refundRef)
                .update();
    }

    /** Changes the status of an order. Legality is the caller's responsibility (see OrderStateMachine). */
    public void updateStatus(long orderId, OrderStatus status) {
        jdbc.sql(OrderSql.UPDATE_STATUS)
                .param(Params.ID, orderId)
                .param(Params.STATUS, status.name())
                .update();
    }

    public Optional<Order> findById(long id) {
        return jdbc.sql(OrderSql.FIND_BY_ID)
                .param(Params.ID, id)
                .query(Order.class)
                .optional();
    }

    /** Finds an order only if it belongs to the customer; a stranger's order looks like it does not exist. */
    public Optional<Order> findByIdAndCustomer(long id, String customer) {
        return jdbc.sql(OrderSql.FIND_BY_ID_AND_CUSTOMER)
                .param(Params.ID, id)
                .param(Params.CUSTOMER, customer)
                .query(Order.class)
                .optional();
    }

    public List<Order> findAll() {
        return jdbc.sql(OrderSql.FIND_ALL).query(Order.class).list();
    }

    public List<Order> findByCustomer(String customer) {
        return jdbc.sql(OrderSql.FIND_BY_CUSTOMER)
                .param(Params.CUSTOMER, customer)
                .query(Order.class)
                .list();
    }
}
