package com.eish.oms.order;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eish.oms.config.ApiPaths;

import jakarta.validation.Valid;

/**
 * Checkout for the authenticated customer. Access is restricted to the CUSTOMER role by the security path rules.
 */
@RestController
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping(ApiPaths.CHECKOUT)
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(Principal customer, @Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(customer.getName(), request);
    }
}
