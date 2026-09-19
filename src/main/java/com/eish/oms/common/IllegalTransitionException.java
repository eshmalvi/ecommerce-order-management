package com.eish.oms.common;

import java.util.Set;

import com.eish.oms.order.OrderStatus;

/**
 * An order cannot move from its current status to the requested one.
 * Carries the legal targets so the API response can list them.
 */
public class IllegalTransitionException extends BusinessRuleException {

    private final Set<OrderStatus> allowedTransitions;

    public IllegalTransitionException(OrderStatus from, OrderStatus to, Set<OrderStatus> allowedTransitions) {
        super("Cannot move order from " + from + " to " + to);
        this.allowedTransitions = allowedTransitions;
    }

    public Set<OrderStatus> getAllowedTransitions() {
        return allowedTransitions;
    }
}
