package com.eish.oms.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * An order as stored in the {@code orders} table. Money fields are the snapshot taken at checkout;
 * {@code paymentRef} and {@code refundRef} are the payment provider's references, null until they exist.
 */
public record Order(
        Long id,
        String customer,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal total,
        String discountCode,
        String paymentRef,
        String refundRef,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
