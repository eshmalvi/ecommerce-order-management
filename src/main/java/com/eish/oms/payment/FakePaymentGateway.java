package com.eish.oms.payment;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.eish.oms.common.PaymentDeclinedException;

/**
 * In-process stand-in for a payment provider, following the test-card convention real providers use:
 * a card number ending in {@value #DECLINED_SUFFIX} is declined, every other card is approved.
 *
 * <p>Because it is instant and local, checkout can call it inside the database transaction. With a real
 * provider that would hold database locks across a network call, and the design would change to
 * reserve stock, charge outside the transaction, then confirm or release.
 */
@Component
public class FakePaymentGateway implements PaymentGateway {

    /** Any card ending in these digits is declined, e.g. 4000000000000002. */
    public static final String DECLINED_SUFFIX = "0002";

    private static final String PAYMENT_REF_PREFIX = "pay_";
    private static final String REFUND_REF_PREFIX = "re_";

    private static final Logger log = LoggerFactory.getLogger(FakePaymentGateway.class);

    @Override
    public String charge(BigDecimal amount, String cardNumber) {
        if (cardNumber.endsWith(DECLINED_SUFFIX)) {
            log.info("Payment of {} declined for card ending {}", amount, lastFour(cardNumber));
            throw new PaymentDeclinedException("Payment declined by the card issuer");
        }
        String paymentRef = PAYMENT_REF_PREFIX + UUID.randomUUID();
        log.info("Payment of {} approved for card ending {}: {}", amount, lastFour(cardNumber), paymentRef);
        return paymentRef;
    }

    @Override
    public String refund(String paymentRef, BigDecimal amount) {
        String refundRef = REFUND_REF_PREFIX + UUID.randomUUID();
        log.info("Refund of {} for payment {}: {}", amount, paymentRef, refundRef);
        return refundRef;
    }

    /** Only the last four digits ever reach a log line. */
    private static String lastFour(String cardNumber) {
        return cardNumber.substring(Math.max(0, cardNumber.length() - 4));
    }
}
