package com.eish.oms.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eish.oms.common.IllegalTransitionException;
import com.eish.oms.common.NotFoundException;
import com.eish.oms.inventory.InventoryRepository;
import com.eish.oms.payment.PaymentGateway;

/**
 * A customer returns a delivered order: the full amount is refunded, every line goes back to the warehouse
 * it shipped from, and the order ends in RETURNED.
 *
 * <p>This is a compensating action, not a rollback: the original order stays on record and the refund is a
 * new movement of money with its own reference. Returning the same order twice fails on the state machine
 * (RETURNED has no legal moves), which is what makes the refund idempotent.
 */
@Service
public class ReturnService {

    private final OrderRepository orders;
    private final OrderLineRepository orderLines;
    private final OrderStateMachine stateMachine;
    private final PaymentGateway paymentGateway;
    private final InventoryRepository inventory;
    private final AuditLogRepository auditLog;
    private final OrderService orderService;

    public ReturnService(OrderRepository orders, OrderLineRepository orderLines, OrderStateMachine stateMachine,
                         PaymentGateway paymentGateway, InventoryRepository inventory,
                         AuditLogRepository auditLog, OrderService orderService) {
        this.orders = orders;
        this.orderLines = orderLines;
        this.stateMachine = stateMachine;
        this.paymentGateway = paymentGateway;
        this.inventory = inventory;
        this.auditLog = auditLog;
        this.orderService = orderService;
    }

    /**
     * Returns one of the customer's own orders.
     *
     * @throws NotFoundException          if the order does not exist or belongs to someone else (404)
     * @throws IllegalTransitionException if the order has not been delivered, or was already returned (409)
     */
    @Transactional
    public OrderResponse returnOrder(String customer, long orderId) {
        Order order = orders.findByIdAndCustomer(orderId, customer)
                .orElseThrow(() -> NotFoundException.of("Order", orderId));
        stateMachine.assertCanTransition(order.status(), OrderStatus.RETURNED);

        String refundRef = paymentGateway.refund(order.paymentRef(), order.total());
        for (OrderLineView line : orderLines.findViewsByOrder(orderId)) {
            inventory.restock(line.productId(), line.warehouseId(), line.quantity());
        }

        orders.markReturned(orderId, OrderStatus.RETURNED, refundRef);
        auditLog.record(orderId, AuditType.STATUS,
                OrderStatus.RETURNED + " (refund " + refundRef + " of " + order.total() + ")");
        return orderService.getOrder(orderId);
    }
}
