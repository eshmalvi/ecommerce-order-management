package com.eish.oms.cart;

import java.util.List;

import com.eish.oms.pricing.PriceBreakdown;

/**
 * A customer's cart as returned by the API: the lines plus what checkout would charge for them right now.
 *
 * @param discountCode the code that was applied to the pricing, or null when none was given
 */
public record CartResponse(List<CartLine> items, String discountCode, PriceBreakdown pricing) {
}
