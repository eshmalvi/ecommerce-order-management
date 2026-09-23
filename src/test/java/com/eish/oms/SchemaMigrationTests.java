package com.eish.oms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Flyway applies the schema and seed data, and the database enforces the invariants on its own.
 *
 * <p>This class deliberately spells table and column names out as literals instead of using {@code Db}:
 * it checks that the migration created what the application expects, so the two must be written
 * independently or a rename in both places at once would go unnoticed.
 */
@SpringBootTest
class SchemaMigrationTests {

    private static final int MIGRATION_COUNT = 2;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void appliesBothMigrations() {
        Integer applied = jdbc.sql("select count(*) from flyway_schema_history where success")
                .query(Integer.class).single();

        assertThat(applied).isEqualTo(MIGRATION_COUNT);
    }

    @Test
    void seedsDemoData() {
        assertThat(count("category")).isEqualTo(SeedData.CATEGORY_COUNT);
        assertThat(count("product")).isEqualTo(SeedData.PRODUCT_COUNT);
        assertThat(count("warehouse")).isEqualTo(SeedData.WAREHOUSE_COUNT);
        assertThat(count("inventory")).isEqualTo(SeedData.INVENTORY_ROW_COUNT);
        assertThat(count("discount")).isEqualTo(SeedData.DISCOUNT_COUNT);

        Integer headphoneStock = jdbc.sql("""
                select sum(i.quantity) from inventory i
                join product p on p.id = i.product_id
                where p.sku = :sku
                """)
                .param("sku", SeedData.SKU_HEADPHONES)
                .query(Integer.class).single();
        assertThat(headphoneStock).isEqualTo(SeedData.HEADPHONES_TOTAL);
    }

    @Test
    void databaseRejectsNegativeStock() {
        assertThatThrownBy(() -> jdbc.sql("update inventory set quantity = -1 where id = (select min(id) from inventory)")
                .update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsDuplicateInventoryRow() {
        assertThatThrownBy(() -> jdbc.sql("""
                insert into inventory (product_id, warehouse_id, quantity)
                select product_id, warehouse_id, 1 from inventory limit 1
                """).update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Integer count(String table) {
        return jdbc.sql("select count(*) from " + table).query(Integer.class).single();
    }
}
