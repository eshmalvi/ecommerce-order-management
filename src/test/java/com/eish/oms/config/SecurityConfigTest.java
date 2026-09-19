package com.eish.oms.config;

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

/**
 * The path rules hold regardless of which endpoints exist: Spring Security answers before any controller.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    /** Access was granted: whatever happened next, it was not an authentication or authorization failure. */
    private static final ResultMatcher ACCESS_GRANTED =
            result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403);

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousCanBrowseProducts() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(ACCESS_GRANTED);
    }

    @Test
    void missingCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cart").with(httpBasic("customer", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotUseAdminEndpoints() throws Exception {
        mockMvc.perform(post("/api/admin/categories").with(httpBasic("customer", "customer123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotUseFulfillmentEndpoints() throws Exception {
        mockMvc.perform(patch("/api/fulfillment/orders/1/status").with(httpBasic("customer", "customer123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void staffCannotUseCart() throws Exception {
        mockMvc.perform(get("/api/cart").with(httpBasic("staff", "staff123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotReturnOrders() throws Exception {
        mockMvc.perform(post("/api/orders/1/return").with(httpBasic("admin", "admin123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void eachRoleReachesItsOwnArea() throws Exception {
        mockMvc.perform(post("/api/admin/categories").with(httpBasic("admin", "admin123"))).andExpect(ACCESS_GRANTED);
        mockMvc.perform(get("/api/cart").with(httpBasic("customer", "customer123"))).andExpect(ACCESS_GRANTED);
        mockMvc.perform(patch("/api/fulfillment/orders/1/status").with(httpBasic("staff", "staff123"))).andExpect(ACCESS_GRANTED);
        mockMvc.perform(get("/api/orders").with(httpBasic("staff", "staff123"))).andExpect(ACCESS_GRANTED);
    }

    @Test
    void unknownPathsAreDeniedEvenWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/something-else").with(httpBasic("admin", "admin123")))
                .andExpect(status().isForbidden());
    }
}
