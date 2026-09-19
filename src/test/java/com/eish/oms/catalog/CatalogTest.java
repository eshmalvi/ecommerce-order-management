package com.eish.oms.catalog;

import static com.eish.oms.config.ApiPaths.ADMIN_CATEGORIES;
import static com.eish.oms.config.ApiPaths.ADMIN_PRODUCTS;
import static com.eish.oms.config.ApiPaths.CATEGORIES;
import static com.eish.oms.config.ApiPaths.PRODUCTS;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.eish.oms.AbstractIntegrationTest;
import com.eish.oms.SeedData;

class CatalogTest extends AbstractIntegrationTest {

    @Test
    void anyoneCanBrowseSeededCatalog() throws Exception {
        mockMvc.perform(get(CATEGORIES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(SeedData.CATEGORY_COUNT)));

        mockMvc.perform(get(PRODUCTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(SeedData.PRODUCT_COUNT)));

        mockMvc.perform(get(PRODUCTS).param("categoryId", String.valueOf(categoryId(SeedData.CATEGORY_BOOKS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku").value(SeedData.SKU_DDIA));
    }

    @Test
    void adminCreatesCategoryAndProduct() throws Exception {
        mockMvc.perform(post(ADMIN_CATEGORIES).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name": "Gaming"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Gaming"));

        mockMvc.perform(post(ADMIN_PRODUCTS).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"sku": "SKU-MOUSE", "name": "Gaming Mouse", "price": 49.90, "categoryId": %d}
                                """.formatted(categoryId("Gaming"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-MOUSE"))
                .andExpect(jsonPath("$.price").value(49.90));

        mockMvc.perform(get(PRODUCTS + "/" + productId("SKU-MOUSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gaming Mouse"));
    }

    @Test
    void duplicateSkuIsConflict() throws Exception {
        mockMvc.perform(post(ADMIN_PRODUCTS).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"sku": "%s", "name": "Another", "price": 1.00, "categoryId": %d}
                                """.formatted(SeedData.SKU_HEADPHONES, categoryId(SeedData.CATEGORY_ELECTRONICS))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void unknownCategoryIsNotFound() throws Exception {
        mockMvc.perform(post(ADMIN_PRODUCTS).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"sku": "SKU-X", "name": "X", "price": 1.00, "categoryId": 9999}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Category 9999 not found"));
    }

    @Test
    void invalidProductIsBadRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post(ADMIN_PRODUCTS).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"sku": "", "name": "X", "price": -5, "categoryId": null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.categoryId").exists());
    }

    @Test
    void malformedJsonIsBadRequest() throws Exception {
        mockMvc.perform(post(ADMIN_CATEGORIES).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Malformed request body"));
    }

    @Test
    void wrongParameterTypeIsBadRequest() throws Exception {
        mockMvc.perform(get(PRODUCTS).param("categoryId", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownProductIsNotFound() throws Exception {
        mockMvc.perform(get(PRODUCTS + "/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product 9999 not found"));
    }
}
