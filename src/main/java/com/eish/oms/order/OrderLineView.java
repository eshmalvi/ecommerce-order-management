package com.eish.oms.order;

import java.math.BigDecimal;

/**
 * An order line joined with the names a reader wants: what was bought, from where, at which price.
 * {@code unitPrice} is the price paid at checkout, not the product's current price.
 */
public record OrderLineView(
        Long productId,
        String sku,
        String productName,
        Long warehouseId,
        String warehouseName,
        int quantity,
        BigDecimal unitPrice) {
}
