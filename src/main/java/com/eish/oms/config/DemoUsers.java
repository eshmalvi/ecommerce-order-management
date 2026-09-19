package com.eish.oms.config;

/**
 * Credentials of the three demo users, one per role. Used by {@link SecurityConfig} and by the tests.
 * A real deployment replaces the in-memory users with a user store; nothing else changes.
 */
public final class DemoUsers {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";

    public static final String CUSTOMER_USERNAME = "customer";
    public static final String CUSTOMER_PASSWORD = "customer123";

    public static final String STAFF_USERNAME = "staff";
    public static final String STAFF_PASSWORD = "staff123";

    private DemoUsers() {
    }
}
