package com.eish.oms.discount;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eish.oms.common.NotFoundException;

/**
 * Discount codes: admins create them, the cart and checkout look them up.
 */
@Service
public class DiscountService {

    private final DiscountRepository discounts;

    public DiscountService(DiscountRepository discounts) {
        this.discounts = discounts;
    }

    public Discount create(CreateDiscountRequest request) {
        return discounts.insert(request.code(), request.percentOff());
    }

    public List<Discount> list() {
        return discounts.findAll();
    }

    /** Resolves a code the customer typed. Unknown codes are a 404 so the client can tell the customer. */
    public Discount getByCode(String code) {
        return discounts.findByCode(code).orElseThrow(() -> NotFoundException.of("Discount code", code));
    }
}
