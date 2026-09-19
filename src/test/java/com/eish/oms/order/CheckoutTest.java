package com.eish.oms.order;

import static com.eish.oms.SeedData.DISCOUNT_CODE;
import static com.eish.oms.SeedData.HEADPHONES_EAST;
import static com.eish.oms.SeedData.HEADPHONES_WEST;
import static com.eish.oms.SeedData.SKU_DDIA;
import static com.eish.oms.SeedData.SKU_HEADPHONES;
import static com.eish.oms.SeedData.WAREHOUSE_EAST;
import static com.eish.oms.SeedData.WAREHOUSE_WEST;
import static com.eish.oms.config.ApiPaths.CHECKOUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.eish.oms.AbstractIntegrationTest;
import com.eish.oms.cart.CartItemRepository;
import com.eish.oms.config.DemoUsers;

/**
 * Checkout through the API. Every failure path must leave stock, cart and orders exactly as they were.
 */
class CheckoutTest extends AbstractIntegrationTest {

    private static final String APPROVED_CARD = "4242424242424242";
    private static final String DECLINED_CARD = "4000000000000002";

    @Autowired
    private CartItemRepository cartItems;

    @Test
    void placesAndPaysForTheCartInOneGo() throws Exception {
        // 3 x 99.99 + 45.00 = 344.97; SAVE10 -> 34.50 off; 10% tax on 310.47 = 31.05; total 341.52
        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_HEADPHONES), 3);
        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_DDIA), 1);

        mockMvc.perform(post(CHECKOUT).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(checkoutJson(APPROVED_CARD, DISCOUNT_CODE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(OrderStatus.CONFIRMED.name()))
                .andExpect(jsonPath("$.subtotal").value(344.97))
                .andExpect(jsonPath("$.discountAmount").value(34.50))
                .andExpect(jsonPath("$.taxAmount").value(31.05))
                .andExpect(jsonPath("$.total").value(341.52))
                .andExpect(jsonPath("$.discountCode").value(DISCOUNT_CODE))
                .andExpect(jsonPath("$.paymentRef", startsWith("pay_")))
                .andExpect(jsonPath("$.lines", hasSize(2)))
                .andExpect(jsonPath("$.lines[*].warehouseName", everyItem(is(WAREHOUSE_EAST))))
                .andExpect(jsonPath("$.lines[0].unitPrice").value(99.99))
                .andExpect(jsonPath("$.history[*].message", hasSize(2)))
                .andExpect(jsonPath("$.history[0].message").value(OrderStatus.PLACED.name()))
                .andExpect(jsonPath("$.history[1].message", startsWith(OrderStatus.CONFIRMED.name())));

        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isEqualTo(HEADPHONES_EAST - 3);
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_WEST)).isEqualTo(HEADPHONES_WEST);
        assertThat(cartRows()).isZero();
        assertThat(orderCount()).isEqualTo(1);
    }

    @Test
    void declinedCardRollsEverythingBack() throws Exception {
        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_HEADPHONES), 2);

        mockMvc.perform(post(CHECKOUT).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(checkoutJson(DECLINED_CARD, null)))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.detail").value("Payment declined by the card issuer"));

        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isEqualTo(HEADPHONES_EAST);
        assertThat(cartRows()).isEqualTo(1);
        assertThat(orderCount()).isZero();
    }

    @Test
    void insufficientStockIsConflictAndChangesNothing() throws Exception {
        // 10 exist in total, but no single warehouse holds 7.
        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_HEADPHONES), 7);

        mockMvc.perform(post(CHECKOUT).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(checkoutJson(APPROVED_CARD, null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "Not enough stock for product " + productId(SKU_HEADPHONES) + " (requested 7)"));

        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isEqualTo(HEADPHONES_EAST);
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_WEST)).isEqualTo(HEADPHONES_WEST);
        assertThat(orderCount()).isZero();
    }

    @Test
    void emptyCartIsConflict() throws Exception {
        mockMvc.perform(post(CHECKOUT).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(checkoutJson(APPROVED_CARD, null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cart is empty"));
    }

    @Test
    void unknownDiscountCodeIsNotFoundAndChargesNothing() throws Exception {
        cartItems.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(SKU_DDIA), 1);

        mockMvc.perform(post(CHECKOUT).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(checkoutJson(APPROVED_CARD, "NOPE")))
                .andExpect(status().isNotFound());

        assertThat(orderCount()).isZero();
        assertThat(cartRows()).isEqualTo(1);
    }

    @Test
    void cardNumberIsValidated() throws Exception {
        mockMvc.perform(post(CHECKOUT).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(checkoutJson("12ab", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cardNumber").exists());
    }

    private static String checkoutJson(String card, String discountCode) {
        return discountCode == null
                ? """
                  {"cardNumber": "%s"}
                  """.formatted(card)
                : """
                  {"cardNumber": "%s", "discountCode": "%s"}
                  """.formatted(card, discountCode);
    }
}
