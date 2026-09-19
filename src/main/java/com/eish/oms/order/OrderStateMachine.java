package com.eish.oms.order;

import static com.eish.oms.order.OrderStatus.CONFIRMED;
import static com.eish.oms.order.OrderStatus.DELIVERED;
import static com.eish.oms.order.OrderStatus.PACKED;
import static com.eish.oms.order.OrderStatus.PLACED;
import static com.eish.oms.order.OrderStatus.RETURNED;
import static com.eish.oms.order.OrderStatus.SHIPPED;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.eish.oms.common.IllegalTransitionException;

/**
 * The legal moves of an order's status, as data. This class decides only <em>whether</em> a move is
 * allowed; <em>who</em> may request it is decided by the security rules on the endpoints.
 *
 * <pre>
 * PLACED -> CONFIRMED -> PACKED -> SHIPPED -> DELIVERED -> RETURNED
 * </pre>
 */
@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            PLACED, Set.of(CONFIRMED),
            CONFIRMED, Set.of(PACKED),
            PACKED, Set.of(SHIPPED),
            SHIPPED, Set.of(DELIVERED),
            DELIVERED, Set.of(RETURNED),
            RETURNED, Set.of());

    /** The statuses an order in {@code from} may move to. Empty for a terminal status. */
    public Set<OrderStatus> allowedTransitions(OrderStatus from) {
        return ALLOWED.get(from);
    }

    public boolean canTransition(OrderStatus from, OrderStatus to) {
        return allowedTransitions(from).contains(to);
    }

    /**
     * Rejects an illegal move with the list of legal ones, so the API can tell the caller what would work.
     *
     * @throws IllegalTransitionException when {@code to} is not a legal successor of {@code from}
     */
    public void assertCanTransition(OrderStatus from, OrderStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalTransitionException(from, to, allowedTransitions(from));
        }
    }
}
