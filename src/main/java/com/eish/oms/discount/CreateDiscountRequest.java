package com.eish.oms.discount;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Body of {@code POST /api/admin/discounts}. Codes are upper-case letters and digits, e.g. SAVE10.
 */
public record CreateDiscountRequest(
        @NotBlank @Pattern(regexp = "[A-Z0-9]{2,32}") String code,
        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        @DecimalMax("100.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal percentOff) {
}
