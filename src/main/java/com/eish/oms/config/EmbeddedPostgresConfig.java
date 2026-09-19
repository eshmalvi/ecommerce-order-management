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
@Profile("local")
public class EmbeddedPostgresConfig {

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
                .url(embeddedPostgres.getJdbcUrl("postgres", "postgres"))
                .username("postgres")
                .password("postgres")
                .build();
    }
}
