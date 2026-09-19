package com.eish.oms.discount;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eish.oms.config.ApiPaths;

import jakarta.validation.Valid;

/**
 * Discount management for admins. Access is restricted to the ADMIN role by the security path rules.
 */
@RestController
public class AdminDiscountController {

    private final DiscountService discountService;

    public AdminDiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping(ApiPaths.ADMIN_DISCOUNTS)
    @ResponseStatus(HttpStatus.CREATED)
    public Discount create(@Valid @RequestBody CreateDiscountRequest request) {
        return discountService.create(request);
    }

    @GetMapping(ApiPaths.ADMIN_DISCOUNTS)
    public List<Discount> list() {
        return discountService.list();
    }
}
