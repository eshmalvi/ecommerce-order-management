package com.eish.oms.pipeline;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Published by checkout once an order is confirmed. Listeners run only after the checkout transaction has
 * committed, so a rolled-back checkout never produces one of these downstream.
 *
 * @param warehouseIds the warehouses the order's lines ship from
 */
public record OrderPlacedEvent(long orderId, String customer, BigDecimal total, Set<Long> warehouseIds) {
}
