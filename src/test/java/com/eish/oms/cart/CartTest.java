package com.eish.oms.cart;

import static com.eish.oms.config.ApiPaths.CART;
import static com.eish.oms.config.ApiPaths.CART_ITEMS;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.eish.oms.AbstractIntegrationTest;
import com.eish.oms.SeedData;

class CartTest extends AbstractIntegrationTest {

    @Autowired
    private CartItemRepository cartItems;

    @Test
    void emptyCartCostsNothing() throws Exception {
        mockMvc.perform(get(CART).with(asCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.pricing.total").value(0.00));
    }

    @Test
    void addingTheSameProductTwiceIncrementsOneLine() throws Exception {
        long headphones = productId(SeedData.SKU_HEADPHONES);

        mockMvc.perform(post(CART_ITEMS).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(itemJson(headphones, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(1));

        mockMvc.perform(post(CART_ITEMS).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(itemJson(headphones, 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.items[0].sku").value(SeedData.SKU_HEADPHONES));
    }

    @Test
    void cartIsPricedLikeCheckoutWillBe() throws Exception {
        // 2 x 99.99 + 1 x 45.00 = 244.98; SAVE10 -> 24.50 off; tax 10% on 220.48 = 22.05; total 242.53
        cartItems.addOrIncrement("customer", productId(SeedData.SKU_HEADPHONES), 2);
        cartItems.addOrIncrement("customer", productId(SeedData.SKU_DDIA), 1);

        mockMvc.perform(get(CART).with(asCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.discountCode").doesNotExist())
                .andExpect(jsonPath("$.pricing.subtotal").value(244.98))
                .andExpect(jsonPath("$.pricing.discountAmount").value(0.00))
                .andExpect(jsonPath("$.pricing.taxAmount").value(24.50))
                .andExpect(jsonPath("$.pricing.total").value(269.48));

        mockMvc.perform(get(CART).with(asCustomer()).param("discountCode", SeedData.DISCOUNT_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountCode").value(SeedData.DISCOUNT_CODE))
                .andExpect(jsonPath("$.pricing.discountAmount").value(24.50))
                .andExpect(jsonPath("$.pricing.taxAmount").value(22.05))
                .andExpect(jsonPath("$.pricing.total").value(242.53));
    }

    @Test
    void unknownDiscountCodeIsNotFound() throws Exception {
        mockMvc.perform(get(CART).with(asCustomer()).param("discountCode", "NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Discount code NOPE not found"));
    }

    @Test
    void unknownProductIsNotFound() throws Exception {
        mockMvc.perform(post(CART_ITEMS).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(itemJson(9999, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product 9999 not found"));
    }

    @Test
    void quantityMustBePositive() throws Exception {
        mockMvc.perform(post(CART_ITEMS).with(asCustomer())
                        .contentType(APPLICATION_JSON)
                        .content(itemJson(productId(SeedData.SKU_DDIA), 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void customersOnlySeeTheirOwnCart() throws Exception {
        cartItems.addOrIncrement("someone-else", productId(SeedData.SKU_KEYBOARD), 5);

        mockMvc.perform(get(CART).with(asCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    private static String itemJson(long productId, int quantity) {
        return """
                {"productId": %d, "quantity": %d}
                """.formatted(productId, quantity);
    }
}
