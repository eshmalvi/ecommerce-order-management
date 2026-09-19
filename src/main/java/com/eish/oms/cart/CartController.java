package com.eish.oms.cart;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eish.oms.config.ApiPaths;

import jakarta.validation.Valid;

/**
 * The authenticated customer's cart. Access is restricted to the CUSTOMER role by the security path rules;
 * the customer's username identifies the cart, so nobody can address another customer's cart.
 */
@RestController
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping(ApiPaths.CART_ITEMS)
    public CartResponse addItem(Principal customer, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(customer.getName(), request);
    }

    /** The cart, priced. Pass {@code ?discountCode=} to preview a code before checkout. */
    @GetMapping(ApiPaths.CART)
    public CartResponse getCart(Principal customer, @RequestParam(required = false) String discountCode) {
        return cartService.getCart(customer.getName(), discountCode);
    }
}
