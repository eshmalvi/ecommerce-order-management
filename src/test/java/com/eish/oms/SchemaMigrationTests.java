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
 */
@SpringBootTest
class SchemaMigrationTests {

    @Autowired
    private JdbcClient jdbc;

    @Test
    void appliesBothMigrations() {
        Integer applied = jdbc.sql("select count(*) from flyway_schema_history where success")
                .query(Integer.class).single();

        assertThat(applied).isEqualTo(2);
    }

    @Test
    void seedsDemoData() {
        assertThat(count("category")).isEqualTo(2);
        assertThat(count("product")).isEqualTo(3);
        assertThat(count("warehouse")).isEqualTo(2);
        assertThat(count("inventory")).isEqualTo(5);
        assertThat(count("discount")).isEqualTo(1);

        Integer headphoneStock = jdbc.sql("""
                select sum(i.quantity) from inventory i
                join product p on p.id = i.product_id
                where p.sku = 'SKU-HEADPHONES'
                """).query(Integer.class).single();
        assertThat(headphoneStock).isEqualTo(10);
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
