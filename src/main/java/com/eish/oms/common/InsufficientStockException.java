package com.eish.oms.common;

/**
 * No warehouse has enough stock of a product for the requested quantity.
 */
public class InsufficientStockException extends BusinessRuleException {

    public InsufficientStockException(long productId, int requested) {
        super("Not enough stock for product " + productId + " (requested " + requested + ")");
    }
}
