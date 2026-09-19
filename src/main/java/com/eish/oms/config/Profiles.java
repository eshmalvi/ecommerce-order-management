package com.eish.oms.config;

/**
 * Spring profile names. The active profile is set in application.yml.
 */
public final class Profiles {

    /** Embedded PostgreSQL inside the JVM; the default. */
    public static final String LOCAL = "local";

    /** External PostgreSQL configured through DB_URL, DB_USER and DB_PASSWORD. */
    public static final String POSTGRES = "postgres";

    private Profiles() {
    }
}
