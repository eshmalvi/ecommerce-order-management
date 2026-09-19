package com.eish.oms.pipeline;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.eish.oms.order.AuditLogRepository;
import com.eish.oms.order.AuditType;

/**
 * Fulfillment routing: asks each warehouse involved to start picking the order.
 * Here that is an entry in the order's history; in production it would be a message to the warehouse system.
 */
@Component
public class RoutingListener {

    private final AuditLogRepository auditLog;

    public RoutingListener(AuditLogRepository auditLog) {
        this.auditLog = auditLog;
    }

    /**
     * AFTER_COMMIT: runs only once the checkout transaction is durable. {@code @Async}: runs on the pipeline
     * pool, off the customer's request thread. REQUIRES_NEW: the pool thread has no transaction, so open one.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(OrderPlacedEvent event) {
        for (Long warehouseId : event.warehouseIds()) {
            auditLog.record(event.orderId(), AuditType.ROUTING,
                    "Fulfillment requested from warehouse " + warehouseId);
        }
    }
}
