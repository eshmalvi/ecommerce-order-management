package com.eish.oms.catalog;

import static com.eish.oms.common.Db.Product.CATEGORY_ID;
import static com.eish.oms.common.Db.Product.COLUMNS;
import static com.eish.oms.common.Db.Product.ID;
import static com.eish.oms.common.Db.Product.NAME;
import static com.eish.oms.common.Db.Product.PRICE;
import static com.eish.oms.common.Db.Product.SKU;
import static com.eish.oms.common.Db.Product.TABLE;
import static com.eish.oms.common.Params.bind;

import com.eish.oms.common.Params;

/**
 * SQL statements for the {@code product} table. Names come from {@code Db}; the grammar stays literal.
 */
final class ProductSql {

    static final String INSERT = """
            insert into %s (%s, %s, %s, %s)
            values (%s, %s, %s, %s)
            returning %s
            """.formatted(TABLE, SKU, NAME, PRICE, CATEGORY_ID,
                    bind(Params.SKU), bind(Params.NAME), bind(Params.PRICE), bind(Params.CATEGORY_ID),
                    ID);

    static final String FIND_ALL = "select %s from %s order by %s"
            .formatted(COLUMNS, TABLE, NAME);

    static final String FIND_BY_CATEGORY = "select %s from %s where %s = %s order by %s"
            .formatted(COLUMNS, TABLE, CATEGORY_ID, bind(Params.CATEGORY_ID), NAME);

    static final String FIND_BY_ID = "select %s from %s where %s = %s"
            .formatted(COLUMNS, TABLE, ID, bind(Params.ID));

    static final String FIND_BY_SKU = "select %s from %s where %s = %s"
            .formatted(COLUMNS, TABLE, SKU, bind(Params.SKU));

    private ProductSql() {
    }
}
