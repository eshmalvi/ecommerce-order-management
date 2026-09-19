package com.eish.oms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Smoke test: the application context starts and talks to the embedded PostgreSQL.
 */
@SpringBootTest
class OrderManagementApplicationTests {

    @Autowired
    private JdbcClient jdbc;

    @Test
    void contextStartsAgainstRealPostgres() {
        String version = jdbc.sql("select version()").query(String.class).single();

        assertThat(version).startsWith("PostgreSQL 17");
    }
}
