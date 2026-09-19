package com.eish.oms.catalog;

import java.math.BigDecimal;

/**
 * A sellable product, as stored in the {@code product} table. {@code price} is the current list price;
 * orders keep their own snapshot of the price that was actually paid.
 */
public record Product(Long id, String sku, String name, BigDecimal price, Long categoryId) {
}
