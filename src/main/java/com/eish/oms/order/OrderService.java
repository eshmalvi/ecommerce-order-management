package com.eish.oms.order;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eish.oms.common.NotFoundException;

/**
 * Reading orders. Customers see only their own; staff and admins see all. A customer asking for someone
 * else's order gets a 404, not a 403, so the existence of the order is not revealed.
 */
@Service
public class OrderService {

    private final OrderRepository orders;
    private final OrderLineRepository orderLines;
    private final AuditLogRepository auditLog;

    public OrderService(OrderRepository orders, OrderLineRepository orderLines, AuditLogRepository auditLog) {
        this.orders = orders;
        this.orderLines = orderLines;
        this.auditLog = auditLog;
    }

    /** Any order, for staff and admins. */
    public OrderResponse getOrder(long orderId) {
        Order order = orders.findById(orderId).orElseThrow(() -> NotFoundException.of("Order", orderId));
        return toResponse(order);
    }

    /** One of the customer's own orders. */
    public OrderResponse getOrderForCustomer(String customer, long orderId) {
        Order order = orders.findByIdAndCustomer(orderId, customer)
                .orElseThrow(() -> NotFoundException.of("Order", orderId));
        return toResponse(order);
    }

    public List<OrderResponse> listOrders() {
        return orders.findAll().stream().map(this::toResponse).toList();
    }

    public List<OrderResponse> listOrdersForCustomer(String customer) {
        return orders.findByCustomer(customer).stream().map(this::toResponse).toList();
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.of(order, orderLines.findViewsByOrder(order.id()), auditLog.findByOrder(order.id()));
    }
}
