package com.eish.oms.cart;

import static com.eish.oms.common.Db.CartItem.CUSTOMER;
import static com.eish.oms.common.Db.CartItem.PRODUCT_ID;
import static com.eish.oms.common.Db.CartItem.QUANTITY;
import static com.eish.oms.common.Db.CartItem.TABLE;
import static com.eish.oms.common.Params.bind;

import com.eish.oms.common.Db;
import com.eish.oms.common.Params;

/**
 * SQL statements for the {@code cart_item} table. Names come from {@code Db}; the grammar stays literal.
 */
final class CartSql {

    /**
     * Add a product to a customer's cart, or bump the quantity if it is already there. One statement, so two
     * concurrent adds for the same product cannot lose an increment.
     */
    static final String ADD_OR_INCREMENT = """
            insert into %s (%s, %s, %s)
            values (%s, %s, %s)
            on conflict (%s, %s) do update set %s = %s.%s + excluded.%s
            """.formatted(TABLE, CUSTOMER, PRODUCT_ID, QUANTITY,
                    bind(Params.CUSTOMER), bind(Params.PRODUCT_ID), bind(Params.QUANTITY),
                    CUSTOMER, PRODUCT_ID, QUANTITY, TABLE, QUANTITY, QUANTITY);

    /** Empties a customer's cart, used once the cart has become an order. */
    static final String DELETE_BY_CUSTOMER = "delete from %s where %s = %s"
            .formatted(TABLE, CUSTOMER, bind(Params.CUSTOMER));

    /**
     * A customer's cart joined with the current product details, ready for pricing. Column aliases match
     * the {@link CartLine} record components. Ordered by product id so every checkout locks inventory rows
     * in the same order.
     */
    static final String FIND_LINES_BY_CUSTOMER = """
            select p.%s as product_id, p.%s as sku, p.%s as product_name, p.%s as unit_price, c.%s as quantity
              from %s c
              join %s p on p.%s = c.%s
             where c.%s = %s
             order by p.%s
            """.formatted(Db.Product.ID, Db.Product.SKU, Db.Product.NAME, Db.Product.PRICE, QUANTITY,
                    TABLE,
                    Db.Product.TABLE, Db.Product.ID, PRODUCT_ID,
                    CUSTOMER, bind(Params.CUSTOMER),
                    Db.Product.ID);

    private CartSql() {
    }
}
