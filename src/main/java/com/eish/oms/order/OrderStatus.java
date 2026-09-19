package com.eish.oms.order;

/**
 * Lifecycle of an order. PLACED and CONFIRMED both happen inside the checkout transaction because
 * payment is synchronous; the remaining steps are driven by warehouse staff and, finally, by a customer return.
 */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PACKED,
    SHIPPED,
    DELIVERED,
    RETURNED
}
