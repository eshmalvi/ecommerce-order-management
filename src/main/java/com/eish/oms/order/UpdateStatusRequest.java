package com.eish.oms.order;

import jakarta.validation.constraints.NotNull;

/**
 * Body of {@code PATCH /api/fulfillment/orders/{id}/status}. An unknown status name is a 400.
 */
public record UpdateStatusRequest(@NotNull OrderStatus status) {
}
