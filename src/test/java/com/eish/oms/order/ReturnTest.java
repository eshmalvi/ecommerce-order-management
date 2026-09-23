package com.eish.oms.order;

import static com.eish.oms.SeedData.HEADPHONES_EAST;
import static com.eish.oms.SeedData.HEADPHONES_WEST;
import static com.eish.oms.SeedData.SKU_HEADPHONES;
import static com.eish.oms.SeedData.WAREHOUSE_EAST;
import static com.eish.oms.SeedData.WAREHOUSE_WEST;
import static com.eish.oms.config.ApiPaths.ORDERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.eish.oms.AbstractIntegrationTest;

/**
 * A customer returns a delivered order: full refund, stock back to the warehouse it shipped from,
 * and never twice.
 */
class ReturnTest extends AbstractIntegrationTest {

    @Autowired
    private FulfillmentService fulfillment;

    @Autowired
    private OrderService orders;

    private long orderId;
    private BigDecimal total;

    @BeforeEach
    void placeAnOrderForTwoHeadphones() {
        OrderResponse order = placeOrder(SKU_HEADPHONES, 2);   // taken from WH-EAST, the fullest
        orderId = order.id();
        total = order.total();
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isEqualTo(HEADPHONES_EAST - 2);
    }

    @Test
    void returningADeliveredOrderRefundsAndRestocksTheOriginWarehouse() throws Exception {
        deliver();

        mockMvc.perform(post(returnPath(orderId)).with(asCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.refundRef", startsWith("re_")))
                .andExpect(jsonPath("$.total").value(total.doubleValue()))
                .andExpect(jsonPath("$.history[-1].message", startsWith("RETURNED (refund re_")));

        // Back to the warehouse the units came from; the other warehouse is untouched.
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isEqualTo(HEADPHONES_EAST);
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_WEST)).isEqualTo(HEADPHONES_WEST);
        assertThat(orders.getOrder(orderId).refundRef()).startsWith("re_");
    }

    @Test
    void returningBeforeDeliveryIsRejected() throws Exception {
        mockMvc.perform(post(returnPath(orderId)).with(asCustomer()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cannot move order from CONFIRMED to RETURNED"))
                .andExpect(jsonPath("$.allowedTransitions[0]").value("PACKED"));

        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isEqualTo(HEADPHONES_EAST - 2);
        assertThat(orders.getOrder(orderId).refundRef()).isNull();
    }

    @Test
    void aSecondReturnIsRejectedSoTheRefundHappensOnce() throws Exception {
        deliver();
        mockMvc.perform(post(returnPath(orderId)).with(asCustomer())).andExpect(status().isOk());
        String firstRefund = orders.getOrder(orderId).refundRef();

        mockMvc.perform(post(returnPath(orderId)).with(asCustomer()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cannot move order from RETURNED to RETURNED"))
                .andExpect(jsonPath("$.allowedTransitions", hasSize(0)));

        assertThat(orders.getOrder(orderId).refundRef()).isEqualTo(firstRefund);
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isEqualTo(HEADPHONES_EAST);   // not restocked twice
    }

    @Test
    void onlyTheOwnerCanReturnAndStrangersSeeNotFound() throws Exception {
        deliver();

        mockMvc.perform(post(returnPath(orderId)).with(asStaff())).andExpect(status().isForbidden());
        mockMvc.perform(post(returnPath(orderId)).with(asAdmin())).andExpect(status().isForbidden());

        // A customer asking for an order that is not theirs (here: one that does not exist) gets 404, never 403.
        mockMvc.perform(post(returnPath(9999)).with(asCustomer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Order 9999 not found"));
        mockMvc.perform(get(ORDERS + "/9999").with(asCustomer()))
                .andExpect(status().isNotFound());
    }

    private void deliver() {
        fulfillment.updateStatus(orderId, OrderStatus.PACKED);
        fulfillment.updateStatus(orderId, OrderStatus.SHIPPED);
        fulfillment.updateStatus(orderId, OrderStatus.DELIVERED);
    }

    private static String returnPath(long id) {
        return ORDERS + "/" + id + "/return";
    }
}
