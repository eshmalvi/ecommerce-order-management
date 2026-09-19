package com.eish.oms.pricing;

import java.math.BigDecimal;

/**
 * The two numbers pricing needs from a cart or order line.
 */
public record PricedLine(BigDecimal unitPrice, int quantity) {
}
