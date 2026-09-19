package com.eish.oms.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.eish.oms.config.AppProperties;

/**
 * Pure arithmetic, so no Spring context. Amounts are compared as values, not by scale.
 */
class PricingServiceTest {

    private static final BigDecimal TEN_PERCENT_TAX = new BigDecimal("0.10");

    private final PricingService pricing = new PricingService(new AppProperties(TEN_PERCENT_TAX));

    /** 2 x 99.99 headphones + 1 x 45.00 book. */
    private final List<PricedLine> cart = List.of(
            new PricedLine(new BigDecimal("99.99"), 2),
            new PricedLine(new BigDecimal("45.00"), 1));

    @Test
    void pricesWithoutDiscount() {
        PriceBreakdown result = pricing.price(cart);

        assertMoney(result.subtotal(), "244.98");
        assertMoney(result.discountAmount(), "0.00");
        assertMoney(result.taxAmount(), "24.50");     // 24.498 rounds half up
        assertMoney(result.total(), "269.48");
    }

    @Test
    void appliesPercentageDiscountBeforeTax() {
        PriceBreakdown result = pricing.price(cart, new BigDecimal("10"));

        assertMoney(result.subtotal(), "244.98");
        assertMoney(result.discountAmount(), "24.50");
        assertMoney(result.taxAmount(), "22.05");     // tax on 220.48, not on 244.98
        assertMoney(result.total(), "242.53");
    }

    @Test
    void roundsEachStepSoTheBreakdownReconciles() {
        // 3 x 9.99 = 29.97; 33% off = 9.8901 -> 9.89; tax on 20.08 = 2.008 -> 2.01
        PriceBreakdown result = pricing.price(
                List.of(new PricedLine(new BigDecimal("9.99"), 3)), new BigDecimal("33"));

        assertMoney(result.subtotal(), "29.97");
        assertMoney(result.discountAmount(), "9.89");
        assertMoney(result.taxAmount(), "2.01");
        assertMoney(result.total(), "22.09");
        assertThat(result.total())
                .isEqualByComparingTo(result.subtotal().subtract(result.discountAmount()).add(result.taxAmount()));
    }

    @Test
    void fullDiscountMakesEverythingFree() {
        PriceBreakdown result = pricing.price(cart, new BigDecimal("100"));

        assertMoney(result.discountAmount(), "244.98");
        assertMoney(result.taxAmount(), "0.00");
        assertMoney(result.total(), "0.00");
    }

    @Test
    void emptyCartCostsNothing() {
        PriceBreakdown result = pricing.price(List.of(), new BigDecimal("10"));

        assertMoney(result.subtotal(), "0.00");
        assertMoney(result.discountAmount(), "0.00");
        assertMoney(result.taxAmount(), "0.00");
        assertMoney(result.total(), "0.00");
    }

    @Test
    void taxRateComesFromConfiguration() {
        PricingService noTax = new PricingService(new AppProperties(BigDecimal.ZERO));

        PriceBreakdown result = noTax.price(cart);

        assertMoney(result.taxAmount(), "0.00");
        assertMoney(result.total(), "244.98");
    }

    @Test
    void everyAmountHasTwoDecimals() {
        PriceBreakdown result = pricing.price(cart, new BigDecimal("12.5"));

        assertThat(result.subtotal().scale()).isEqualTo(2);
        assertThat(result.discountAmount().scale()).isEqualTo(2);
        assertThat(result.taxAmount().scale()).isEqualTo(2);
        assertThat(result.total().scale()).isEqualTo(2);
    }

    private static void assertMoney(BigDecimal actual, String expected) {
        assertThat(actual).isEqualByComparingTo(new BigDecimal(expected));
    }
}
