package com.eish.oms.discount;

import static com.eish.oms.config.ApiPaths.ADMIN_DISCOUNTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.eish.oms.AbstractIntegrationTest;
import com.eish.oms.SeedData;
import com.eish.oms.common.NotFoundException;

class DiscountAdminTest extends AbstractIntegrationTest {

    @Autowired
    private DiscountService discountService;

    @Test
    void adminCreatesAndListsDiscounts() throws Exception {
        mockMvc.perform(post(ADMIN_DISCOUNTS).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"code": "SPRING25", "percentOff": 25.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SPRING25"))
                .andExpect(jsonPath("$.percentOff").value(25.00));

        mockMvc.perform(get(ADMIN_DISCOUNTS).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(SeedData.DISCOUNT_COUNT + 1)));
    }

    @Test
    void duplicateCodeIsConflict() throws Exception {
        mockMvc.perform(post(ADMIN_DISCOUNTS).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"code": "%s", "percentOff": 5.00}
                                """.formatted(SeedData.DISCOUNT_CODE)))
                .andExpect(status().isConflict());
    }

    @Test
    void percentageMustBeBetweenZeroExclusiveAndHundredInclusive() throws Exception {
        mockMvc.perform(post(ADMIN_DISCOUNTS).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"code": "ZERO", "percentOff": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.percentOff").exists());

        mockMvc.perform(post(ADMIN_DISCOUNTS).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"code": "TOOMUCH", "percentOff": 100.01}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.percentOff").exists());

        mockMvc.perform(post(ADMIN_DISCOUNTS).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"code": "FREE", "percentOff": 100}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void codeFormatIsValidated() throws Exception {
        mockMvc.perform(post(ADMIN_DISCOUNTS).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"code": "lower case!", "percentOff": 10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").exists());
    }

    @Test
    void serviceResolvesSeededCodeAndRejectsUnknownOnes() {
        Discount seeded = discountService.getByCode(SeedData.DISCOUNT_CODE);
        assertThat(seeded.percentOff()).isEqualByComparingTo(new BigDecimal("10.00"));

        assertThatThrownBy(() -> discountService.getByCode("NOPE"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Discount code NOPE not found");
    }
}
