package com.eish.oms.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Body of {@code POST /api/checkout}.
 *
 * @param cardNumber   12 to 19 digits; with the fake gateway a number ending 0002 is declined
 * @param discountCode optional code to apply, must exist
 */
public record CheckoutRequest(
        @NotBlank @Pattern(regexp = "\\d{12,19}", message = "must be 12 to 19 digits") String cardNumber,
        String discountCode) {
}
