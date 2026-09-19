package com.eish.oms.discount;

/**
 * SQL statements for the {@code discount} table.
 */
final class DiscountSql {

    static final String INSERT = "insert into discount (code, percent_off) values (:code, :percentOff)";

    static final String FIND_ALL = "select code, percent_off from discount order by code";

    static final String FIND_BY_CODE = "select code, percent_off from discount where code = :code";

    private DiscountSql() {
    }
}
