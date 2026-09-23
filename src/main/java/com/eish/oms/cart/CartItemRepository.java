package com.eish.oms.cart;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.eish.oms.common.Params;

/**
 * Data access for the {@code cart_item} table. The SQL lives in {@link CartSql}.
 */
@Repository
public class CartItemRepository {

    private final JdbcClient jdbc;

    public CartItemRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Adds {@code quantity} of a product to the customer's cart, creating the line or incrementing it. */
    public void addOrIncrement(String customer, long productId, int quantity) {
        jdbc.sql(CartSql.ADD_OR_INCREMENT)
                .param(Params.CUSTOMER, customer)
                .param(Params.PRODUCT_ID, productId)
                .param(Params.QUANTITY, quantity)
                .update();
    }

    /** Removes every line from the customer's cart. */
    public void clear(String customer) {
        jdbc.sql(CartSql.DELETE_BY_CUSTOMER)
                .param(Params.CUSTOMER, customer)
                .update();
    }

    /** The customer's cart lines with current product details, ordered by product id. */
    public List<CartLine> findLines(String customer) {
        return jdbc.sql(CartSql.FIND_LINES_BY_CUSTOMER)
                .param(Params.CUSTOMER, customer)
                .query(CartLine.class)
                .list();
    }
}
