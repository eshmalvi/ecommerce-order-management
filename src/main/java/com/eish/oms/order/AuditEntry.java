package com.eish.oms.order;

import java.time.OffsetDateTime;

/**
 * One line of an order's history, as stored in the {@code audit_log} table.
 */
public record AuditEntry(AuditType type, String message, OffsetDateTime at) {
}
