package com.eish.oms.pricing;

import java.math.BigDecimal;

/**
 * The money side of a cart or order. Always holds: {@code total = subtotal - discountAmount + taxAmount}.
 *
 * @param subtotal       sum of unit price times quantity over all lines
 * @param discountAmount the percentage discount taken off the subtotal (zero when no code was applied)
 * @param taxAmount      tax on the discounted subtotal
 * @param total          what the customer pays
 */
public record PriceBreakdown(BigDecimal subtotal, BigDecimal discountAmount, BigDecimal taxAmount, BigDecimal total) {
}
