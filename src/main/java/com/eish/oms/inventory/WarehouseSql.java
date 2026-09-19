package com.eish.oms.inventory;

/**
 * SQL statements for the {@code warehouse} table.
 */
final class WarehouseSql {

    static final String INSERT = "insert into warehouse (name) values (:name) returning id";

    static final String FIND_ALL = "select id, name from warehouse order by name";

    static final String FIND_BY_ID = "select id, name from warehouse where id = :id";

    private WarehouseSql() {
    }
}
