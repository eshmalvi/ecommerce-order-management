package com.eish.oms.order;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eish.oms.common.IllegalTransitionException;
import com.eish.oms.common.NotFoundException;

/**
 * Warehouse staff move orders through fulfillment: packed, shipped, delivered.
 * The state machine decides whether the move is legal; this service additionally limits staff to the
 * fulfillment statuses, so they cannot confirm payment or register a return.
 */
@Service
public class FulfillmentService {

    /** The statuses warehouse staff are allowed to set. */
    static final Set<OrderStatus> STAFF_TARGETS =
            EnumSet.of(OrderStatus.PACKED, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final OrderRepository orders;
    private final OrderStateMachine stateMachine;
    private final AuditLogRepository auditLog;
    private final OrderService orderService;

    public FulfillmentService(OrderRepository orders, OrderStateMachine stateMachine,
                              AuditLogRepository auditLog, OrderService orderService) {
        this.orders = orders;
        this.stateMachine = stateMachine;
        this.auditLog = auditLog;
        this.orderService = orderService;
    }

    /**
     * Moves an order to the next fulfillment status.
     *
     * @throws NotFoundException          if the order does not exist (404)
     * @throws IllegalTransitionException if the move is not legal from the current status, or the target is
     *                                    not a fulfillment status; the response lists what staff could do instead (409)
     */
    @Transactional
    public OrderResponse updateStatus(long orderId, OrderStatus target) {
        Order order = orders.findById(orderId).orElseThrow(() -> NotFoundException.of("Order", orderId));

        Set<OrderStatus> allowedForStaff = EnumSet.copyOf(stateMachine.allowedTransitions(order.status()));
        allowedForStaff.retainAll(STAFF_TARGETS);
        if (!allowedForStaff.contains(target)) {
            throw new IllegalTransitionException(order.status(), target, allowedForStaff);
        }

        orders.updateStatus(orderId, target);
        auditLog.record(orderId, AuditType.STATUS, target.name());
        return orderService.getOrder(orderId);
    }
}
