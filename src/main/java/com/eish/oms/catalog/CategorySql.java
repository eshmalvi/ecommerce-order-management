package com.eish.oms.catalog;

/**
 * SQL statements for the {@code category} table.
 */
final class CategorySql {

    static final String INSERT = "insert into category (name) values (:name) returning id";

    static final String FIND_ALL = "select id, name from category order by name";

    static final String FIND_BY_ID = "select id, name from category where id = :id";

    private CategorySql() {
    }
}
