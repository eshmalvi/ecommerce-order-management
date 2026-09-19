package com.eish.oms.order;

/**
 * What kind of event an {@code audit_log} row records.
 */
public enum AuditType {
    /** A status change, written in the same transaction as the change itself. */
    STATUS,
    /** Fulfillment was requested from a warehouse (asynchronous pipeline). */
    ROUTING,
    /** The customer was notified (asynchronous pipeline). */
    NOTIFICATION,
    /** Summary entry written by the audit step of the asynchronous pipeline. */
    AUDIT
}
