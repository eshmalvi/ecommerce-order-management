package com.eish.oms.pipeline;

import static com.eish.oms.SeedData.SKU_DDIA;
import static com.eish.oms.SeedData.SKU_HEADPHONES;
import static com.eish.oms.SeedData.SKU_KEYBOARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.eish.oms.AbstractIntegrationTest;
import com.eish.oms.cart.CartItemRepository;
import com.eish.oms.common.PaymentDeclinedException;
import com.eish.oms.config.DemoUsers;
import com.eish.oms.order.AuditEntry;
import com.eish.oms.order.AuditType;
import com.eish.oms.order.CheckoutRequest;
import com.eish.oms.order.CheckoutService;
import com.eish.oms.order.OrderResponse;
import com.eish.oms.order.OrderService;

/**
 * The post-checkout pipeline runs after the commit, off the request thread, and never for a checkout that
 * rolled back.
 */
class PipelineTest extends AbstractIntegrationTest {

    private static final Duration PIPELINE_TIMEOUT = Duration.ofSeconds(5);

    @Autowired
    private CheckoutService checkout;

    @Autowired
    private OrderService orders;

    @Autowired
    private CartItemRepository cartItems;

    @Test
    void checkoutReturnsBeforeThePipelineRunsAndThePipelineCatchesUp() {
        OrderResponse response = placeOrder(SKU_KEYBOARD, 1);

        // The response carries only what the transaction itself wrote.
        List<AuditType> atResponseTime = response.history().stream().map(AuditEntry::type).toList();
        assertThat(atResponseTime).containsOnly(AuditType.STATUS);

        // Shortly after, the three listeners have each written their row.
        await().atMost(PIPELINE_TIMEOUT).untilAsserted(() -> {
            List<AuditEntry> history = orders.getOrder(response.id()).history();
            assertThat(history).filteredOn(e -> e.type() == AuditType.ROUTING).hasSize(1);
            assertThat(history).filteredOn(e -> e.type() == AuditType.NOTIFICATION).hasSize(1);
            assertThat(history).filteredOn(e -> e.type() == AuditType.AUDIT).hasSize(1);
        });
    }

    @Test
    void routingRequestsFulfillmentFromEveryWarehouseTheOrderShipsFrom() {
        // Headphones come from WH-EAST (fullest); the book only exists in WH-EAST too, so one routing row.
        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_HEADPHONES), 1);
        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_DDIA), 1);
        OrderResponse oneWarehouse = checkout.checkout(DemoUsers.CUSTOMER_USERNAME, new CheckoutRequest(APPROVED_CARD, null));

        await().atMost(PIPELINE_TIMEOUT).untilAsserted(() ->
                assertThat(orders.getOrder(oneWarehouse.id()).history())
                        .filteredOn(e -> e.type() == AuditType.ROUTING)
                        .extracting(AuditEntry::message)
                        .containsExactly("Fulfillment requested from warehouse " + oneWarehouse.lines().get(0).warehouseId()));

        // Drain east's remaining headphones (5 left) so the next headphone line must come from WH-WEST,
        // while the book still comes from WH-EAST: two warehouses, two routing rows.
        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_HEADPHONES), 5);
        checkout.checkout(DemoUsers.CUSTOMER_USERNAME, new CheckoutRequest(APPROVED_CARD, null));

        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_HEADPHONES), 1);
        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_DDIA), 1);
        OrderResponse twoWarehouses = checkout.checkout(DemoUsers.CUSTOMER_USERNAME, new CheckoutRequest(APPROVED_CARD, null));

        assertThat(twoWarehouses.lines()).extracting(l -> l.warehouseName()).containsExactlyInAnyOrder("WH-WEST", "WH-EAST");
        await().atMost(PIPELINE_TIMEOUT).untilAsserted(() ->
                assertThat(orders.getOrder(twoWarehouses.id()).history())
                        .filteredOn(e -> e.type() == AuditType.ROUTING)
                        .hasSize(2));
    }

    @Test
    void aDeclinedCheckoutTriggersNothingDownstream() {
        int auditRowsBefore = auditRows(AuditType.ROUTING) + auditRows(AuditType.NOTIFICATION) + auditRows(AuditType.AUDIT);
        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_KEYBOARD), 1);

        assertThatThrownBy(() -> checkout.checkout(DemoUsers.CUSTOMER_USERNAME, new CheckoutRequest(DECLINED_CARD, null)))
                .isInstanceOf(PaymentDeclinedException.class);

        // Give the pool a moment in which a wrongly-fired listener would have written; nothing may appear.
        await().pollDelay(Duration.ofMillis(500)).atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            int auditRowsAfter = auditRows(AuditType.ROUTING) + auditRows(AuditType.NOTIFICATION) + auditRows(AuditType.AUDIT);
            assertThat(auditRowsAfter).isEqualTo(auditRowsBefore);
            assertThat(orderCount()).isZero();
        });
    }

    @Test
    void notificationAndAuditRowsDescribeTheOrder() {
        OrderResponse response = placeOrder(SKU_KEYBOARD, 2);

        await().atMost(PIPELINE_TIMEOUT).untilAsserted(() -> {
            List<AuditEntry> history = orders.getOrder(response.id()).history();
            assertThat(history).filteredOn(e -> e.type() == AuditType.NOTIFICATION)
                    .extracting(AuditEntry::message)
                    .containsExactly("Order confirmation sent to " + DemoUsers.CUSTOMER_USERNAME);
            assertThat(history).filteredOn(e -> e.type() == AuditType.AUDIT)
                    .extracting(AuditEntry::message)
                    .containsExactly("Checkout completed by " + DemoUsers.CUSTOMER_USERNAME + ", charged " + response.total());
        });
    }
}
