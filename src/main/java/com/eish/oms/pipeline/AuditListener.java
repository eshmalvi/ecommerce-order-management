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
 * Audit logging: records that checkout completed, with the amount charged.
 * In production this entry would also go to a separate, append-only audit store.
 */
@Component
public class AuditListener {

    private final AuditLogRepository auditLog;

    public AuditListener(AuditLogRepository auditLog) {
        this.auditLog = auditLog;
    }

    /** See {@link RoutingListener#on} for why the three annotations are needed together. */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(OrderPlacedEvent event) {
        auditLog.record(event.orderId(), AuditType.AUDIT,
                "Checkout completed by " + event.customer() + ", charged " + event.total());
    }
}
