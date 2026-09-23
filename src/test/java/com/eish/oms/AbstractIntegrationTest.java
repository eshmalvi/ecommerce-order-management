package com.eish.oms;

import static com.eish.oms.common.Params.bind;
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

import com.eish.oms.cart.CartItemRepository;
import com.eish.oms.common.Db;
import com.eish.oms.common.Params;
import com.eish.oms.config.DemoUsers;
import com.eish.oms.order.AuditType;
import com.eish.oms.order.CheckoutRequest;
import com.eish.oms.order.CheckoutService;
import com.eish.oms.order.OrderResponse;

/**
 * Base for integration tests: full application context on the embedded PostgreSQL, MockMvc for HTTP,
 * and the database reset to the seed data before every test method.
 *
 * <p>Deliberately not {@code @Transactional}: the tests exercise real commits, concurrent transactions
 * and after-commit listeners, all of which a test-managed transaction would hide.
 *
 * <p>The query helpers below read the database directly, using the same {@link Db} vocabulary as the
 * application, so a schema rename breaks them at compile time rather than at run time.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql({"/reset.sql", "/seed.sql"})
public abstract class AbstractIntegrationTest {

    private static final String WAREHOUSE_PARAM = "warehouse";

    /** Test card numbers understood by the fake payment gateway. */
    protected static final String APPROVED_CARD = "4242424242424242";
    protected static final String DECLINED_CARD = "4000000000000002";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcClient jdbc;

    @Autowired
    private CartItemRepository cartItemsForSetup;

    @Autowired
    private CheckoutService checkoutForSetup;

    /**
     * Every confirmed order eventually gets exactly one NOTIFICATION row from the asynchronous pipeline.
     * Waiting for that here means no listener is still writing when the next test truncates the tables.
     */
    @AfterEach
    void drainPipeline() {
        await().atMost(Duration.ofSeconds(10)).until(() -> notificationRows() == paidOrders());
    }

    // ---- test data setup ----

    /**
     * Places and pays for an order of {@code quantity} units of one product for the demo customer, through
     * the real checkout service. Used by tests that start after checkout (fulfillment, returns, pipeline).
     */
    protected OrderResponse placeOrder(String sku, int quantity) {
        cartItemsForSetup.addOrIncrement(DemoUsers.CUSTOMER_USERNAME, productId(sku), quantity);
        return checkoutForSetup.checkout(DemoUsers.CUSTOMER_USERNAME, new CheckoutRequest(APPROVED_CARD, null));
    }

    // ---- credentials ----

    protected static RequestPostProcessor asAdmin() {
        return httpBasic(DemoUsers.ADMIN_USERNAME, DemoUsers.ADMIN_PASSWORD);
    }

    protected static RequestPostProcessor asCustomer() {
        return httpBasic(DemoUsers.CUSTOMER_USERNAME, DemoUsers.CUSTOMER_PASSWORD);
    }

    protected static RequestPostProcessor asStaff() {
        return httpBasic(DemoUsers.STAFF_USERNAME, DemoUsers.STAFF_PASSWORD);
    }

    // ---- row counts ----

    protected int orderCount() {
        return count(Db.Orders.TABLE);
    }

    protected int paidOrders() {
        return count(Db.Orders.TABLE, Db.Orders.PAYMENT_REF + " is not null");
    }

    protected int cartRows() {
        return count(Db.CartItem.TABLE);
    }

    protected int notificationRows() {
        return auditRows(AuditType.NOTIFICATION);
    }

    protected int auditRows(AuditType type) {
        return jdbc.sql("select count(*) from %s where %s = %s"
                        .formatted(Db.AuditLog.TABLE, Db.AuditLog.TYPE, bind(Params.TYPE)))
                .param(Params.TYPE, type.name())
                .query(Integer.class)
                .single();
    }

    private int count(String table) {
        return jdbc.sql("select count(*) from " + table).query(Integer.class).single();
    }

    private int count(String table, String whereClause) {
        return jdbc.sql("select count(*) from %s where %s".formatted(table, whereClause))
                .query(Integer.class)
                .single();
    }

    // ---- seed lookups ----

    protected long productId(String sku) {
        return idWhere(Db.Product.TABLE, Db.Product.ID, Db.Product.SKU, Params.SKU, sku);
    }

    protected long categoryId(String name) {
        return idWhere(Db.Category.TABLE, Db.Category.ID, Db.Category.NAME, Params.NAME, name);
    }

    protected long warehouseId(String name) {
        return idWhere(Db.Warehouse.TABLE, Db.Warehouse.ID, Db.Warehouse.NAME, Params.NAME, name);
    }

    private long idWhere(String table, String idColumn, String column, String param, String value) {
        return jdbc.sql("select %s from %s where %s = %s".formatted(idColumn, table, column, bind(param)))
                .param(param, value)
                .query(Long.class)
                .single();
    }

    // ---- stock ----

    protected int totalStock(String sku) {
        return jdbc.sql("""
                select coalesce(sum(i.%s), 0)
                  from %s i
                  join %s p on p.%s = i.%s
                 where p.%s = %s
                """.formatted(Db.Inventory.QUANTITY,
                        Db.Inventory.TABLE,
                        Db.Product.TABLE, Db.Product.ID, Db.Inventory.PRODUCT_ID,
                        Db.Product.SKU, bind(Params.SKU)))
                .param(Params.SKU, sku)
                .query(Integer.class)
                .single();
    }

    protected int stock(String sku, String warehouse) {
        return jdbc.sql("""
                select i.%s
                  from %s i
                  join %s p on p.%s = i.%s
                  join %s w on w.%s = i.%s
                 where p.%s = %s and w.%s = %s
                """.formatted(Db.Inventory.QUANTITY,
                        Db.Inventory.TABLE,
                        Db.Product.TABLE, Db.Product.ID, Db.Inventory.PRODUCT_ID,
                        Db.Warehouse.TABLE, Db.Warehouse.ID, Db.Inventory.WAREHOUSE_ID,
                        Db.Product.SKU, bind(Params.SKU), Db.Warehouse.NAME, bind(WAREHOUSE_PARAM)))
                .param(Params.SKU, sku)
                .param(WAREHOUSE_PARAM, warehouse)
                .query(Integer.class)
                .single();
    }
}
