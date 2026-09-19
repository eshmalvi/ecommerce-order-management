package com.eish.oms.common;

/**
 * The payment gateway refused the charge. Mapped to HTTP 402.
 */
public class PaymentDeclinedException extends RuntimeException {

    public PaymentDeclinedException(String message) {
        super(message);
    }
}
