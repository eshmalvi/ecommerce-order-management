package com.eish.oms.order;

import java.security.Principal;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eish.oms.config.ApiPaths;

/**
 * Returns by the authenticated customer. Access is restricted to the CUSTOMER role by the security path rules;
 * the service only finds orders that belong to the caller.
 */
@RestController
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping(ApiPaths.ORDER_RETURN)
    public OrderResponse returnOrder(Principal customer, @PathVariable long id) {
        return returnService.returnOrder(customer.getName(), id);
    }
}
