package com.eish.oms.common;

/**
 * The request is well-formed but conflicts with the current state of the system
 * (empty cart, not enough stock, illegal status change). Mapped to HTTP 409.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
