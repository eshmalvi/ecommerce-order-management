package com.eish.oms.catalog;

import static com.eish.oms.common.Db.Category.COLUMNS;
import static com.eish.oms.common.Db.Category.ID;
import static com.eish.oms.common.Db.Category.NAME;
import static com.eish.oms.common.Db.Category.TABLE;
import static com.eish.oms.common.Params.bind;

import com.eish.oms.common.Params;

/**
 * SQL statements for the {@code category} table. Names come from {@code Db}; the grammar stays literal.
 */
final class CategorySql {

    static final String INSERT = "insert into %s (%s) values (%s) returning %s"
            .formatted(TABLE, NAME, bind(Params.NAME), ID);

    static final String FIND_ALL = "select %s from %s order by %s"
            .formatted(COLUMNS, TABLE, NAME);

    static final String FIND_BY_ID = "select %s from %s where %s = %s"
            .formatted(COLUMNS, TABLE, ID, bind(Params.ID));

    private CategorySql() {
    }
}
