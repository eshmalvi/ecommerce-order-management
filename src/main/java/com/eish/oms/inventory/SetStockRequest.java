package com.eish.oms.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Body of {@code PUT /api/admin/inventory}: the absolute stock level of one product in one warehouse.
 */
public record SetStockRequest(
        @NotNull Long productId,
        @NotNull Long warehouseId,
        @NotNull @Min(0) Integer quantity) {
}
