package com.eish.oms.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * Starts a real PostgreSQL inside the JVM for local runs and tests, so nothing needs to be installed.
 * Active only under the "local" profile; the "postgres" profile points at an external database instead.
 */
@Configuration
@Profile(Profiles.LOCAL)
public class EmbeddedPostgresConfig {

    /** The superuser and default database that every embedded PostgreSQL starts with. */
    private static final String EMBEDDED_USER = "postgres";
    private static final String EMBEDDED_DATABASE = "postgres";
    /** Embedded PostgreSQL trusts local connections; the password is required by the pool but not checked. */
    private static final String EMBEDDED_PASSWORD = "postgres";

    /**
     * Boots an embedded PostgreSQL on a random free port. Its data directory is temporary,
     * so every start is a clean database that Flyway migrates and seeds.
     */
    @Bean(destroyMethod = "close")
    public EmbeddedPostgres embeddedPostgres() throws IOException {
        return EmbeddedPostgres.start();
    }

    /**
     * Wraps the embedded instance in a Hikari connection pool so connection behaviour matches production.
     */
    @Bean
    public DataSource dataSource(EmbeddedPostgres embeddedPostgres) {
        return DataSourceBuilder.create()
                .url(embeddedPostgres.getJdbcUrl(EMBEDDED_USER, EMBEDDED_DATABASE))
                .username(EMBEDDED_USER)
                .password(EMBEDDED_PASSWORD)
                .build();
    }
}
