package com.eish.oms.order;

/**
 * SQL statements for the {@code audit_log} table.
 */
final class AuditSql {

    static final String INSERT = """
            insert into audit_log (order_id, type, message)
            values (:orderId, :type, :message)
            """;

    static final String FIND_BY_ORDER = """
            select type, message, at
              from audit_log
             where order_id = :orderId
             order by at, id
            """;

    private AuditSql() {
    }
}
