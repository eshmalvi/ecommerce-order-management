package com.eish.oms.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/admin/categories}.
 */
public record CreateCategoryRequest(@NotBlank @Size(max = 100) String name) {
}
