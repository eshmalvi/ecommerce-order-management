package com.eish.oms.config;

/**
 * The three roles of the system. Spring Security stores them with a {@code ROLE_} prefix;
 * these are the bare names used with {@code hasRole(...)}.
 */
public final class Roles {

    /** Manages catalog, warehouses, inventory and discounts. */
    public static final String ADMIN = "ADMIN";

    /** Browses, buys, tracks and returns orders. */
    public static final String CUSTOMER = "CUSTOMER";

    /** Warehouse staff: moves orders through fulfillment. */
    public static final String STAFF = "STAFF";

    private Roles() {
    }
}
