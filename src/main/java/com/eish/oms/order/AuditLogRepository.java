package com.eish.oms.order;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Data access for the {@code audit_log} table. The SQL lives in {@link AuditSql}.
 */
@Repository
public class AuditLogRepository {

    private final JdbcClient jdbc;

    public AuditLogRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Appends one entry to an order's history. Runs in the caller's transaction when there is one. */
    public void record(long orderId, AuditType type, String message) {
        jdbc.sql(AuditSql.INSERT)
                .param("orderId", orderId)
                .param("type", type.name())
                .param("message", message)
                .update();
    }

    public List<AuditEntry> findByOrder(long orderId) {
        return jdbc.sql(AuditSql.FIND_BY_ORDER)
                .param("orderId", orderId)
                .query(AuditEntry.class)
                .list();
    }
}
