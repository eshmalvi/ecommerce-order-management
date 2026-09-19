package com.eish.oms.order;

/**
 * SQL statements for the {@code orders} table.
 */
final class OrderSql {

    private static final String COLUMNS = """
            id, customer, status, subtotal, discount_amount, tax_amount, total,
            discount_code, payment_ref, refund_ref, created_at, updated_at""";

    static final String INSERT = """
            insert into orders (customer, status, subtotal, discount_amount, tax_amount, total, discount_code)
            values (:customer, :status, :subtotal, :discountAmount, :taxAmount, :total, :discountCode)
            returning id
            """;

    /** Payment went through: record the reference and move to the next status. */
    static final String CONFIRM = """
            update orders
               set status = :status, payment_ref = :paymentRef, updated_at = now()
             where id = :id
            """;

    static final String FIND_BY_ID =
            "select " + COLUMNS + " from orders where id = :id";

    static final String FIND_BY_ID_AND_CUSTOMER =
            "select " + COLUMNS + " from orders where id = :id and customer = :customer";

    static final String FIND_ALL =
            "select " + COLUMNS + " from orders order by created_at desc, id desc";

    static final String FIND_BY_CUSTOMER =
            "select " + COLUMNS + " from orders where customer = :customer order by created_at desc, id desc";

    private OrderSql() {
    }
}
