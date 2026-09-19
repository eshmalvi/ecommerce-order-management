package com.eish.oms.cart;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.eish.oms.catalog.ProductRepository;
import com.eish.oms.common.NotFoundException;
import com.eish.oms.discount.DiscountService;
import com.eish.oms.pricing.PriceBreakdown;
import com.eish.oms.pricing.PricedLine;
import com.eish.oms.pricing.PricingService;

/**
 * A customer's cart. The customer is identified by the authenticated username.
 */
@Service
public class CartService {

    private final CartItemRepository cartItems;
    private final ProductRepository products;
    private final DiscountService discounts;
    private final PricingService pricing;

    public CartService(CartItemRepository cartItems, ProductRepository products,
                       DiscountService discounts, PricingService pricing) {
        this.cartItems = cartItems;
        this.products = products;
        this.discounts = discounts;
        this.pricing = pricing;
    }

    /** Adds a product to the cart (or increases its quantity) and returns the updated cart. */
    public CartResponse addItem(String customer, AddCartItemRequest request) {
        products.findById(request.productId())
                .orElseThrow(() -> NotFoundException.of("Product", request.productId()));
        cartItems.addOrIncrement(customer, request.productId(), request.quantity());
        return getCart(customer, null);
    }

    /**
     * The cart with a price breakdown, optionally with a discount code applied so the customer sees
     * the final price before paying. Checkout uses the same pricing, so the two never disagree.
     */
    public CartResponse getCart(String customer, String discountCode) {
        List<CartLine> lines = cartItems.findLines(customer);
        BigDecimal percentOff = discountCode == null
                ? BigDecimal.ZERO
                : discounts.getByCode(discountCode).percentOff();
        PriceBreakdown breakdown = pricing.price(toPricedLines(lines), percentOff);
        return new CartResponse(lines, discountCode, breakdown);
    }

    /** Converts cart lines into the shape the pricing engine understands. */
    public static List<PricedLine> toPricedLines(List<CartLine> lines) {
        return lines.stream()
                .map(line -> new PricedLine(line.unitPrice(), line.quantity()))
                .toList();
    }
}
