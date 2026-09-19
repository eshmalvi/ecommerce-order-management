package com.eish.oms;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Base for integration tests: full application context on the embedded PostgreSQL, MockMvc for HTTP,
 * and the database reset to the seed data before every test method.
 *
 * <p>Deliberately not {@code @Transactional}: the tests exercise real commits, concurrent transactions
 * and after-commit listeners, all of which a test-managed transaction would hide.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql({"/reset.sql", "/seed.sql"})
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcClient jdbc;

    protected static RequestPostProcessor asAdmin() {
        return httpBasic("admin", "admin123");
    }

    protected static RequestPostProcessor asCustomer() {
        return httpBasic("customer", "customer123");
    }

    protected static RequestPostProcessor asStaff() {
        return httpBasic("staff", "staff123");
    }

    protected long productId(String sku) {
        return jdbc.sql("select id from product where sku = :sku").param("sku", sku).query(Long.class).single();
    }

    protected long categoryId(String name) {
        return jdbc.sql("select id from category where name = :name").param("name", name).query(Long.class).single();
    }

    protected long warehouseId(String name) {
        return jdbc.sql("select id from warehouse where name = :name").param("name", name).query(Long.class).single();
    }

    protected int stock(String sku, String warehouse) {
        return jdbc.sql("""
                select i.quantity from inventory i
                join product p on p.id = i.product_id
                join warehouse w on w.id = i.warehouse_id
                where p.sku = :sku and w.name = :warehouse
                """)
                .param("sku", sku)
                .param("warehouse", warehouse)
                .query(Integer.class)
                .single();
    }
}
