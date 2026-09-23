package com.eish.oms.order;

import static com.eish.oms.SeedData.HEADPHONES_TOTAL;
import static com.eish.oms.SeedData.SKU_HEADPHONES;
import static com.eish.oms.SeedData.WAREHOUSE_EAST;
import static com.eish.oms.SeedData.WAREHOUSE_WEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.eish.oms.AbstractIntegrationTest;
import com.eish.oms.Parallel;
import com.eish.oms.cart.CartItemRepository;
import com.eish.oms.common.InsufficientStockException;

/**
 * The no-oversell guarantee end to end: fifty customers each check out one unit of a product that only has
 * ten. Exactly ten orders exist afterwards, the other forty customers keep their carts, and stock is zero.
 */
class CheckoutConcurrencyTest extends AbstractIntegrationTest {

    private static final int CUSTOMERS = 50;

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CartItemRepository cartItems;

    @RepeatedTest(3)
    void fiftyConcurrentCheckoutsProduceExactlyTenOrders() throws Exception {
        long headphones = productId(SKU_HEADPHONES);
        for (int i = 0; i < CUSTOMERS; i++) {
            cartItems.addOrIncrement(customer(i), headphones, 1);
        }

        List<Boolean> ordered = Parallel.run(CUSTOMERS, i -> () -> {
            try {
                OrderResponse order = checkoutService.checkout(customer(i), new CheckoutRequest(APPROVED_CARD, null));
                assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
                return true;
            } catch (InsufficientStockException outOfStock) {
                return false;
            }
        });

        long winners = ordered.stream().filter(Boolean::booleanValue).count();
        assertThat(winners).isEqualTo(HEADPHONES_TOTAL);
        assertThat(orderCount()).isEqualTo(HEADPHONES_TOTAL);
        assertThat(cartRows()).isEqualTo(CUSTOMERS - HEADPHONES_TOTAL);   // losers keep their carts
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isZero();
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_WEST)).isZero();

        // The pipeline kept up: one notification per confirmed order, none for the failed attempts.
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(notificationRows()).isEqualTo(HEADPHONES_TOTAL));
    }

    private static String customer(int i) {
        return "customer-" + i;
    }
}
