package com.eish.oms.discount;

import java.math.BigDecimal;

/**
 * A percentage-off code, as stored in the {@code discount} table. The code itself is the primary key.
 */
public record Discount(String code, BigDecimal percentOff) {
}
