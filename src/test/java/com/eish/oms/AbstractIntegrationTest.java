package com.eish.oms;

import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.eish.oms.config.DemoUsers;

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

    /**
     * Every confirmed order eventually gets exactly one NOTIFICATION row from the asynchronous pipeline.
     * Waiting for that here means no listener is still writing when the next test truncates the tables.
     */
    @AfterEach
    void drainPipeline() {
        await().atMost(Duration.ofSeconds(10)).until(() -> notificationRows() == paidOrders());
    }

    protected int paidOrders() {
        return jdbc.sql("select count(*) from orders where payment_ref is not null").query(Integer.class).single();
    }

    protected int notificationRows() {
        return jdbc.sql("select count(*) from audit_log where type = 'NOTIFICATION'").query(Integer.class).single();
    }

    protected int auditRows(String type) {
        return jdbc.sql("select count(*) from audit_log where type = :type").param("type", type)
                .query(Integer.class).single();
    }

    protected int orderCount() {
        return jdbc.sql("select count(*) from orders").query(Integer.class).single();
    }

    protected int cartRows() {
        return jdbc.sql("select count(*) from cart_item").query(Integer.class).single();
    }

    protected static RequestPostProcessor asAdmin() {
        return httpBasic(DemoUsers.ADMIN_USERNAME, DemoUsers.ADMIN_PASSWORD);
    }

    protected static RequestPostProcessor asCustomer() {
        return httpBasic(DemoUsers.CUSTOMER_USERNAME, DemoUsers.CUSTOMER_PASSWORD);
    }

    protected static RequestPostProcessor asStaff() {
        return httpBasic(DemoUsers.STAFF_USERNAME, DemoUsers.STAFF_PASSWORD);
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

    protected int totalStock(String sku) {
        return jdbc.sql("""
                select coalesce(sum(i.quantity), 0) from inventory i
                join product p on p.id = i.product_id
                where p.sku = :sku
                """)
                .param("sku", sku)
                .query(Integer.class)
                .single();
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
