package com.eish.oms.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Body of {@code POST /api/cart/items}.
 */
public record AddCartItemRequest(@NotNull Long productId, @NotNull @Positive Integer quantity) {
}
