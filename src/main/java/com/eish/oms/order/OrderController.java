package com.eish.oms.order;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.eish.oms.config.ApiPaths;
import com.eish.oms.config.Roles;

/**
 * Order tracking. Customers see their own orders; staff and admins see every order.
 */
@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(ApiPaths.ORDERS)
    public List<OrderResponse> listOrders(Authentication caller) {
        return Roles.hasRole(caller, Roles.CUSTOMER)
                ? orderService.listOrdersForCustomer(caller.getName())
                : orderService.listOrders();
    }

    @GetMapping(ApiPaths.ORDER_BY_ID)
    public OrderResponse getOrder(Authentication caller, @PathVariable long id) {
        return Roles.hasRole(caller, Roles.CUSTOMER)
                ? orderService.getOrderForCustomer(caller.getName(), id)
                : orderService.getOrder(id);
    }
}
