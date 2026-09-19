package com.eish.oms.payment;

import java.math.BigDecimal;

import com.eish.oms.common.PaymentDeclinedException;

/**
 * The port through which the shop takes and returns money. Checkout and returns depend on this interface
 * only; the implementation can be the in-process fake used here or an adapter for a real provider.
 *
 * <p>A real adapter would pass the order id as the provider's idempotency key, so a retried call after a
 * network timeout returns the original result instead of charging twice.
 */
public interface PaymentGateway {

    /**
     * Charges the card and returns the provider's payment reference.
     *
     * @throws PaymentDeclinedException when the provider refuses the charge
     */
    String charge(BigDecimal amount, String cardNumber);

    /**
     * Refunds a previous charge and returns the provider's refund reference.
     */
    String refund(String paymentRef, BigDecimal amount);
}
