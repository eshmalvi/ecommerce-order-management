package com.eish.oms.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/admin/warehouses}.
 */
public record CreateWarehouseRequest(@NotBlank @Size(max = 100) String name) {
}
