package com.eish.oms.discount;

import static com.eish.oms.common.Db.Discount.CODE;
import static com.eish.oms.common.Db.Discount.COLUMNS;
import static com.eish.oms.common.Db.Discount.PERCENT_OFF;
import static com.eish.oms.common.Db.Discount.TABLE;
import static com.eish.oms.common.Params.bind;

import com.eish.oms.common.Params;

/**
 * SQL statements for the {@code discount} table. Names come from {@code Db}; the grammar stays literal.
 */
final class DiscountSql {

    static final String INSERT = "insert into %s (%s, %s) values (%s, %s)"
            .formatted(TABLE, CODE, PERCENT_OFF, bind(Params.CODE), bind(Params.PERCENT_OFF));

    static final String FIND_ALL = "select %s from %s order by %s"
            .formatted(COLUMNS, TABLE, CODE);

    static final String FIND_BY_CODE = "select %s from %s where %s = %s"
            .formatted(COLUMNS, TABLE, CODE, bind(Params.CODE));

    private DiscountSql() {
    }
}
