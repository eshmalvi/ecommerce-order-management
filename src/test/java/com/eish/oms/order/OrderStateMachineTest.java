package com.eish.oms.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.eish.oms.common.IllegalTransitionException;

/**
 * Pure logic, no Spring context. Every legal move is listed once; every other pair must be rejected.
 */
class OrderStateMachineTest {

    private final OrderStateMachine machine = new OrderStateMachine();

    @ParameterizedTest(name = "{0} -> {1} is legal")
    @CsvSource({
            "PLACED,    CONFIRMED",
            "CONFIRMED, PACKED",
            "PACKED,    SHIPPED",
            "SHIPPED,   DELIVERED",
            "DELIVERED, RETURNED",
    })
    void theLifecycleMovesForwardOneStepAtATime(OrderStatus from, OrderStatus to) {
        assertThat(machine.canTransition(from, to)).isTrue();
        machine.assertCanTransition(from, to);   // must not throw
    }

    @ParameterizedTest(name = "{0} -> {1} is rejected")
    @MethodSource("illegalPairs")
    void everyOtherPairIsRejectedWithTheLegalAlternatives(OrderStatus from, OrderStatus to) {
        assertThat(machine.canTransition(from, to)).isFalse();

        assertThatThrownBy(() -> machine.assertCanTransition(from, to))
                .isInstanceOf(IllegalTransitionException.class)
                .hasMessage("Cannot move order from " + from + " to " + to)
                .extracting(ex -> ((IllegalTransitionException) ex).getAllowedTransitions())
                .isEqualTo(machine.allowedTransitions(from));
    }

    /** All 36 pairs minus the 5 legal ones: 31 rejections, including every self-transition and every backwards move. */
    static Stream<Arguments> illegalPairs() {
        Set<String> legal = Set.of("PLACED>CONFIRMED", "CONFIRMED>PACKED", "PACKED>SHIPPED",
                "SHIPPED>DELIVERED", "DELIVERED>RETURNED");
        return EnumSet.allOf(OrderStatus.class).stream()
                .flatMap(from -> EnumSet.allOf(OrderStatus.class).stream()
                        .filter(to -> !legal.contains(from + ">" + to))
                        .map(to -> Arguments.of(from, to)));
    }

    @Test
    void returnedIsTerminal() {
        assertThat(machine.allowedTransitions(OrderStatus.RETURNED)).isEmpty();
    }

    @Test
    void everyStatusExceptTheLastHasExactlyOneSuccessor() {
        for (OrderStatus status : OrderStatus.values()) {
            int expected = status == OrderStatus.RETURNED ? 0 : 1;
            assertThat(machine.allowedTransitions(status)).hasSize(expected);
        }
    }
}
