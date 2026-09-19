package com.eish.oms.cart;

/**
 * SQL statements for the {@code cart_item} table.
 */
final class CartSql {

    /**
     * Add a product to a customer's cart, or bump the quantity if it is already there. One statement, so two
     * concurrent adds for the same product cannot lose an increment.
     */
    static final String ADD_OR_INCREMENT = """
            insert into cart_item (customer, product_id, quantity)
            values (:customer, :productId, :quantity)
            on conflict (customer, product_id) do update set quantity = cart_item.quantity + excluded.quantity
            """;

    /** Empties a customer's cart, used once the cart has become an order. */
    static final String DELETE_BY_CUSTOMER = "delete from cart_item where customer = :customer";

    /** A customer's cart joined with the current product details, ready for pricing. */
    static final String FIND_LINES_BY_CUSTOMER = """
            select p.id as product_id, p.sku, p.name as product_name, p.price as unit_price, c.quantity
              from cart_item c
              join product p on p.id = c.product_id
             where c.customer = :customer
             order by p.id
            """;

    private CartSql() {
    }
}
