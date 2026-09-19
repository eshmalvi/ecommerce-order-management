package com.eish.oms.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The one place that decides how money is rounded: two decimals, half up, matching {@code numeric(12,2)}
 * in the database. Every monetary amount in the system passes through {@link #round(BigDecimal)}.
 */
public final class Money {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private Money() {
    }

    /** Rounds to two decimals, half up. */
    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING);
    }
}
