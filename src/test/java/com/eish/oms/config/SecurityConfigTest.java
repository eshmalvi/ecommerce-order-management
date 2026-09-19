package com.eish.oms.config;

import static com.eish.oms.config.ApiPaths.ADMIN_CATEGORIES;
import static com.eish.oms.config.ApiPaths.CART;
import static com.eish.oms.config.ApiPaths.FULFILLMENT;
import static com.eish.oms.config.ApiPaths.ORDERS;
import static com.eish.oms.config.ApiPaths.PRODUCTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * The path rules hold regardless of which endpoints exist: Spring Security answers before any controller.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    /** Access was granted: whatever happened next, it was not an authentication or authorization failure. */
    private static final ResultMatcher ACCESS_GRANTED =
            result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403);

    private static final RequestPostProcessor ADMIN = httpBasic(DemoUsers.ADMIN_USERNAME, DemoUsers.ADMIN_PASSWORD);
    private static final RequestPostProcessor CUSTOMER = httpBasic(DemoUsers.CUSTOMER_USERNAME, DemoUsers.CUSTOMER_PASSWORD);
    private static final RequestPostProcessor STAFF = httpBasic(DemoUsers.STAFF_USERNAME, DemoUsers.STAFF_PASSWORD);

    private static final String SOME_ORDER_STATUS = FULFILLMENT + "/orders/1/status";
    private static final String SOME_ORDER_RETURN = ORDERS + "/1/return";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousCanBrowseProducts() throws Exception {
        mockMvc.perform(get(PRODUCTS)).andExpect(ACCESS_GRANTED);
    }

    @Test
    void missingCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(get(CART)).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        mockMvc.perform(get(CART).with(httpBasic(DemoUsers.CUSTOMER_USERNAME, "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotUseAdminEndpoints() throws Exception {
        mockMvc.perform(post(ADMIN_CATEGORIES).with(CUSTOMER)).andExpect(status().isForbidden());
    }

    @Test
    void customerCannotUseFulfillmentEndpoints() throws Exception {
        mockMvc.perform(patch(SOME_ORDER_STATUS).with(CUSTOMER)).andExpect(status().isForbidden());
    }

    @Test
    void staffCannotUseCart() throws Exception {
        mockMvc.perform(get(CART).with(STAFF)).andExpect(status().isForbidden());
    }

    @Test
    void adminCannotReturnOrders() throws Exception {
        mockMvc.perform(post(SOME_ORDER_RETURN).with(ADMIN)).andExpect(status().isForbidden());
    }

    @Test
    void eachRoleReachesItsOwnArea() throws Exception {
        mockMvc.perform(post(ADMIN_CATEGORIES).with(ADMIN)).andExpect(ACCESS_GRANTED);
        mockMvc.perform(get(CART).with(CUSTOMER)).andExpect(ACCESS_GRANTED);
        mockMvc.perform(patch(SOME_ORDER_STATUS).with(STAFF)).andExpect(ACCESS_GRANTED);
        mockMvc.perform(get(ORDERS).with(STAFF)).andExpect(ACCESS_GRANTED);
    }

    @Test
    void unknownPathsAreDeniedEvenWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/something-else").with(ADMIN)).andExpect(status().isForbidden());
    }
}
