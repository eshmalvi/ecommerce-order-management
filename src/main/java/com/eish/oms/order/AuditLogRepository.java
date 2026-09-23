package com.eish.oms.order;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.eish.oms.common.Params;

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
                .param(Params.ORDER_ID, orderId)
                .param(Params.TYPE, type.name())
                .param(Params.MESSAGE, message)
                .update();
    }

    public List<AuditEntry> findByOrder(long orderId) {
        return jdbc.sql(AuditSql.FIND_BY_ORDER)
                .param(Params.ORDER_ID, orderId)
                .query(AuditEntry.class)
                .list();
    }
}
