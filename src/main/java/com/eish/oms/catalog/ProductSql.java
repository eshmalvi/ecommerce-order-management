package com.eish.oms.catalog;

/**
 * SQL statements for the {@code product} table. Kept together so the repository reads as plain method calls.
 */
final class ProductSql {

    private static final String COLUMNS = "id, sku, name, price, category_id";

    static final String INSERT = """
            insert into product (sku, name, price, category_id)
            values (:sku, :name, :price, :categoryId)
            returning id
            """;

    static final String FIND_ALL = "select " + COLUMNS + " from product order by name";

    static final String FIND_BY_CATEGORY = "select " + COLUMNS + " from product where category_id = :categoryId order by name";

    static final String FIND_BY_ID = "select " + COLUMNS + " from product where id = :id";

    static final String FIND_BY_SKU = "select " + COLUMNS + " from product where sku = :sku";

    private ProductSql() {
    }
}
