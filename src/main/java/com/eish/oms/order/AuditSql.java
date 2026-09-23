package com.eish.oms.order;

import static com.eish.oms.common.Db.AuditLog.AT;
import static com.eish.oms.common.Db.AuditLog.ID;
import static com.eish.oms.common.Db.AuditLog.MESSAGE;
import static com.eish.oms.common.Db.AuditLog.ORDER_ID;
import static com.eish.oms.common.Db.AuditLog.TABLE;
import static com.eish.oms.common.Db.AuditLog.TYPE;
import static com.eish.oms.common.Params.bind;

import com.eish.oms.common.Params;

/**
 * SQL statements for the {@code audit_log} table. Names come from {@code Db}; the grammar stays literal.
 */
final class AuditSql {

    static final String INSERT = """
            insert into %s (%s, %s, %s)
            values (%s, %s, %s)
            """.formatted(TABLE, ORDER_ID, TYPE, MESSAGE,
                    bind(Params.ORDER_ID), bind(Params.TYPE), bind(Params.MESSAGE));

    /** An order's history, oldest first. Columns match the {@link AuditEntry} record. */
    static final String FIND_BY_ORDER = """
            select %s, %s, %s
              from %s
             where %s = %s
             order by %s, %s
            """.formatted(TYPE, MESSAGE, AT,
                    TABLE,
                    ORDER_ID, bind(Params.ORDER_ID),
                    AT, ID);

    private AuditSql() {
    }
}
