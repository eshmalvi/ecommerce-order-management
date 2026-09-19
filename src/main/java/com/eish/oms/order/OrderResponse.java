package com.eish.oms.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * An order as returned by the API: the order itself, its lines, and its history.
 */
public record OrderResponse(
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
        OffsetDateTime updatedAt,
        List<OrderLineView> lines,
        List<AuditEntry> history) {

    static OrderResponse of(Order order, List<OrderLineView> lines, List<AuditEntry> history) {
        return new OrderResponse(
                order.id(), order.customer(), order.status(),
                order.subtotal(), order.discountAmount(), order.taxAmount(), order.total(),
                order.discountCode(), order.paymentRef(), order.refundRef(),
                order.createdAt(), order.updatedAt(),
                lines, history);
    }
}
