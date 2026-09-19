package com.eish.oms.cart;

import java.math.BigDecimal;

/**
 * One line of a customer's cart, joined with the product it refers to. {@code unitPrice} is the
 * product's current price; the cart always shows what checkout would charge right now.
 */
public record CartLine(Long productId, String sku, String productName, BigDecimal unitPrice, int quantity) {
}
