package com.eish.oms.order;

import static com.eish.oms.common.Db.Orders.COLUMNS;
import static com.eish.oms.common.Db.Orders.CREATED_AT;
import static com.eish.oms.common.Db.Orders.CUSTOMER;
import static com.eish.oms.common.Db.Orders.DISCOUNT_AMOUNT;
import static com.eish.oms.common.Db.Orders.DISCOUNT_CODE;
import static com.eish.oms.common.Db.Orders.ID;
import static com.eish.oms.common.Db.Orders.PAYMENT_REF;
import static com.eish.oms.common.Db.Orders.REFUND_REF;
import static com.eish.oms.common.Db.Orders.STATUS;
import static com.eish.oms.common.Db.Orders.SUBTOTAL;
import static com.eish.oms.common.Db.Orders.TABLE;
import static com.eish.oms.common.Db.Orders.TAX_AMOUNT;
import static com.eish.oms.common.Db.Orders.TOTAL;
import static com.eish.oms.common.Db.Orders.UPDATED_AT;
import static com.eish.oms.common.Params.bind;

import com.eish.oms.common.Params;

/**
 * SQL statements for the {@code orders} table. Names come from {@code Db}; the grammar stays literal.
 */
final class OrderSql {

    /** Newest first; id breaks ties between orders created in the same instant. */
    private static final String NEWEST_FIRST = "order by %s desc, %s desc".formatted(CREATED_AT, ID);

    static final String INSERT = """
            insert into %s (%s, %s, %s, %s, %s, %s, %s)
            values (%s, %s, %s, %s, %s, %s, %s)
            returning %s
            """.formatted(TABLE, CUSTOMER, STATUS, SUBTOTAL, DISCOUNT_AMOUNT, TAX_AMOUNT, TOTAL, DISCOUNT_CODE,
                    bind(Params.CUSTOMER), bind(Params.STATUS), bind(Params.SUBTOTAL), bind(Params.DISCOUNT_AMOUNT),
                    bind(Params.TAX_AMOUNT), bind(Params.TOTAL), bind(Params.DISCOUNT_CODE),
                    ID);

    /** Payment went through: record the reference and move to the next status. */
    static final String CONFIRM = """
            update %s
               set %s = %s, %s = %s, %s = now()
             where %s = %s
            """.formatted(TABLE,
                    STATUS, bind(Params.STATUS), PAYMENT_REF, bind(Params.PAYMENT_REF), UPDATED_AT,
                    ID, bind(Params.ID));

    /** The order came back and the money went back: record the refund reference with the new status. */
    static final String MARK_RETURNED = """
            update %s
               set %s = %s, %s = %s, %s = now()
             where %s = %s
            """.formatted(TABLE,
                    STATUS, bind(Params.STATUS), REFUND_REF, bind(Params.REFUND_REF), UPDATED_AT,
                    ID, bind(Params.ID));

    /** A plain status change, used by fulfillment. */
    static final String UPDATE_STATUS = """
            update %s
               set %s = %s, %s = now()
             where %s = %s
            """.formatted(TABLE,
                    STATUS, bind(Params.STATUS), UPDATED_AT,
                    ID, bind(Params.ID));

    static final String FIND_BY_ID = "select %s from %s where %s = %s"
            .formatted(COLUMNS, TABLE, ID, bind(Params.ID));

    /** Finds an order only if it belongs to the customer; a stranger's order looks like it does not exist. */
    static final String FIND_BY_ID_AND_CUSTOMER = "select %s from %s where %s = %s and %s = %s"
            .formatted(COLUMNS, TABLE, ID, bind(Params.ID), CUSTOMER, bind(Params.CUSTOMER));

    static final String FIND_ALL = "select %s from %s %s"
            .formatted(COLUMNS, TABLE, NEWEST_FIRST);

    static final String FIND_BY_CUSTOMER = "select %s from %s where %s = %s %s"
            .formatted(COLUMNS, TABLE, CUSTOMER, bind(Params.CUSTOMER), NEWEST_FIRST);

    private OrderSql() {
    }
}
