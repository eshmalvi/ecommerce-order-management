package com.eish.oms.order;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.eish.oms.config.ApiPaths;

import jakarta.validation.Valid;

/**
 * Fulfillment updates by warehouse staff. Access is restricted to the STAFF role by the security path rules.
 */
@RestController
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;

    public FulfillmentController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @PatchMapping(ApiPaths.FULFILLMENT_ORDER_STATUS)
    public OrderResponse updateStatus(@PathVariable long id, @Valid @RequestBody UpdateStatusRequest request) {
        return fulfillmentService.updateStatus(id, request.status());
    }
}
