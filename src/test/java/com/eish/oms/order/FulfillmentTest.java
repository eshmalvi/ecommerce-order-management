package com.eish.oms.order;

import static com.eish.oms.SeedData.SKU_KEYBOARD;
import static com.eish.oms.config.ApiPaths.FULFILLMENT;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eish.oms.AbstractIntegrationTest;

/**
 * Warehouse staff move a paid order through fulfillment. Who may call is the security layer's decision;
 * whether the move is legal is the state machine's. Both are exercised here through the API.
 */
class FulfillmentTest extends AbstractIntegrationTest {

    private long orderId;

    @BeforeEach
    void placeAnOrder() {
        orderId = placeOrder(SKU_KEYBOARD, 1).id();
    }

    @Test
    void staffWalkTheOrderThroughPackedShippedDelivered() throws Exception {
        move(OrderStatus.PACKED).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PACKED"));
        move(OrderStatus.SHIPPED).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
        move(OrderStatus.DELIVERED).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.history[?(@.type == 'STATUS')].message",
                        hasSize(5)));   // PLACED, CONFIRMED, PACKED, SHIPPED, DELIVERED
    }

    @Test
    void skippingAStepIsRejectedWithTheLegalMoveListed() throws Exception {
        move(OrderStatus.DELIVERED)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cannot move order from CONFIRMED to DELIVERED"))
                .andExpect(jsonPath("$.allowedTransitions", contains("PACKED")));
    }

    @Test
    void movingBackwardsIsRejected() throws Exception {
        move(OrderStatus.PACKED).andExpect(status().isOk());
        move(OrderStatus.SHIPPED).andExpect(status().isOk());

        move(OrderStatus.PACKED)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.allowedTransitions", contains("DELIVERED")));
    }

    @Test
    void staffCannotRegisterAReturn() throws Exception {
        move(OrderStatus.PACKED).andExpect(status().isOk());
        move(OrderStatus.SHIPPED).andExpect(status().isOk());
        move(OrderStatus.DELIVERED).andExpect(status().isOk());

        // DELIVERED -> RETURNED is legal for the state machine, but not for staff: the allowed list is empty.
        move(OrderStatus.RETURNED)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.allowedTransitions", hasSize(0)));
    }

    @Test
    void staffCannotConfirmPayment() throws Exception {
        move(OrderStatus.CONFIRMED)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.allowedTransitions", contains("PACKED")));
    }

    @Test
    void unknownStatusNameIsBadRequest() throws Exception {
        mockMvc.perform(patch(statusPath(orderId)).with(asStaff())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"status": "LOST"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Malformed request body"));
    }

    @Test
    void missingStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch(statusPath(orderId)).with(asStaff())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());
    }

    @Test
    void unknownOrderIsNotFound() throws Exception {
        mockMvc.perform(patch(statusPath(9999)).with(asStaff())
                        .contentType(APPLICATION_JSON)
                        .content(statusJson(OrderStatus.PACKED)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Order 9999 not found"));
    }

    @Test
    void customersAndAdminsCannotTouchFulfillment() throws Exception {
        mockMvc.perform(patch(statusPath(orderId)).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(statusJson(OrderStatus.PACKED)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch(statusPath(orderId)).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content(statusJson(OrderStatus.PACKED)))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions move(OrderStatus target) throws Exception {
        return mockMvc.perform(patch(statusPath(orderId)).with(asStaff())
                .contentType(APPLICATION_JSON)
                .content(statusJson(target)));
    }

    private static String statusPath(long id) {
        return FULFILLMENT + "/orders/" + id + "/status";
    }

    private static String statusJson(OrderStatus status) {
        return """
                {"status": "%s"}
                """.formatted(status);
    }
}
