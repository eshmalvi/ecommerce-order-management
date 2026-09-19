package com.eish.oms.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.eish.oms.order.AuditLogRepository;
import com.eish.oms.order.AuditType;

/**
 * Customer notification: tells the customer the order is confirmed.
 * Here that is a log line plus an entry in the order's history; in production it would be an email or push.
 */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final AuditLogRepository auditLog;

    public NotificationListener(AuditLogRepository auditLog) {
        this.auditLog = auditLog;
    }

    /** See {@link RoutingListener#on} for why the three annotations are needed together. */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(OrderPlacedEvent event) {
        log.info("Notifying {}: order {} confirmed, total {}", event.customer(), event.orderId(), event.total());
        auditLog.record(event.orderId(), AuditType.NOTIFICATION, "Order confirmation sent to " + event.customer());
    }
}
