package com.eish.oms.pricing;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.eish.oms.common.Money;
import com.eish.oms.config.AppProperties;

/**
 * Computes what a set of lines costs. Stateless and free of I/O, so it is unit-tested directly.
 *
 * <p>Order of operations: subtotal, then the percentage discount off the subtotal, then tax on the
 * discounted amount (you pay tax on the price you actually pay), then the total. Every step is rounded
 * to two decimals so the breakdown always reconciles to the cent.
 */
@Service
public class PricingService {

    private final BigDecimal taxRate;

    public PricingService(AppProperties properties) {
        this.taxRate = properties.taxRate();
    }

    /** Prices the lines with no discount. */
    public PriceBreakdown price(List<PricedLine> lines) {
        return price(lines, BigDecimal.ZERO);
    }

    /**
     * Prices the lines with a percentage discount.
     *
     * @param percentOff discount as a percentage of the subtotal, for example 10 for 10%; zero for none
     */
    public PriceBreakdown price(List<PricedLine> lines, BigDecimal percentOff) {
        BigDecimal subtotal = Money.round(lines.stream()
                .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal discountAmount = Money.round(subtotal.multiply(percentOff).divide(Money.ONE_HUNDRED, Money.ROUNDING));
        BigDecimal discounted = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = Money.round(discounted.multiply(taxRate));
        BigDecimal total = discounted.add(taxAmount);

        return new PriceBreakdown(subtotal, discountAmount, taxAmount, total);
    }
}
