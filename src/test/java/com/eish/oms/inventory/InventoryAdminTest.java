package com.eish.oms.inventory;

import static com.eish.oms.config.ApiPaths.ADMIN_INVENTORY;
import static com.eish.oms.config.ApiPaths.ADMIN_WAREHOUSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.eish.oms.AbstractIntegrationTest;
import com.eish.oms.SeedData;

class InventoryAdminTest extends AbstractIntegrationTest {

    @Test
    void adminCreatesAndListsWarehouses() throws Exception {
        mockMvc.perform(post(ADMIN_WAREHOUSES).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name": "WH-NORTH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("WH-NORTH"));

        mockMvc.perform(get(ADMIN_WAREHOUSES).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(SeedData.WAREHOUSE_COUNT + 1)));
    }

    @Test
    void duplicateWarehouseNameIsConflict() throws Exception {
        mockMvc.perform(post(ADMIN_WAREHOUSES).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name": "%s"}
                                """.formatted(SeedData.WAREHOUSE_EAST)))
                .andExpect(status().isConflict());
    }

    @Test
    void settingStockCreatesTheRowThenOverwritesIt() throws Exception {
        long book = productId(SeedData.SKU_DDIA);
        long west = warehouseId(SeedData.WAREHOUSE_WEST);

        // The book is not stocked in the west warehouse in the seed data.
        mockMvc.perform(put(ADMIN_INVENTORY).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content(stockJson(book, west, 25)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(25));
        assertThat(stock(SeedData.SKU_DDIA, SeedData.WAREHOUSE_WEST)).isEqualTo(25);

        // Same product and warehouse again: the row is overwritten, not duplicated.
        mockMvc.perform(put(ADMIN_INVENTORY).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content(stockJson(book, west, 7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(7));
        assertThat(stock(SeedData.SKU_DDIA, SeedData.WAREHOUSE_WEST)).isEqualTo(7);

        mockMvc.perform(get(ADMIN_INVENTORY).with(asAdmin()).param("productId", String.valueOf(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void negativeStockIsBadRequest() throws Exception {
        mockMvc.perform(put(ADMIN_INVENTORY).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content(stockJson(productId(SeedData.SKU_DDIA), warehouseId(SeedData.WAREHOUSE_EAST), -1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void unknownProductOrWarehouseIsNotFound() throws Exception {
        mockMvc.perform(put(ADMIN_INVENTORY).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content(stockJson(9999, warehouseId(SeedData.WAREHOUSE_EAST), 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product 9999 not found"));

        mockMvc.perform(put(ADMIN_INVENTORY).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content(stockJson(productId(SeedData.SKU_DDIA), 9999, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Warehouse 9999 not found"));
    }

    @Test
    void listsSeededStockAcrossWarehouses() throws Exception {
        mockMvc.perform(get(ADMIN_INVENTORY).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(SeedData.INVENTORY_ROW_COUNT)));

        assertThat(stock(SeedData.SKU_HEADPHONES, SeedData.WAREHOUSE_EAST)).isEqualTo(SeedData.HEADPHONES_EAST);
        assertThat(stock(SeedData.SKU_HEADPHONES, SeedData.WAREHOUSE_WEST)).isEqualTo(SeedData.HEADPHONES_WEST);
    }

    private static String stockJson(long productId, long warehouseId, int quantity) {
        return """
                {"productId": %d, "warehouseId": %d, "quantity": %d}
                """.formatted(productId, warehouseId, quantity);
    }
}
