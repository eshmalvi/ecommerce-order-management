package com.eish.oms.config;

/**
 * Every URL path of the API, defined once. Controllers map to these and {@link SecurityConfig}
 * authorizes on them, so the two can never drift apart.
 */
public final class ApiPaths {

    private static final String API = "/api";

    // Public browsing
    public static final String CATEGORIES = API + "/categories";
    public static final String PRODUCTS = API + "/products";
    public static final String PRODUCT_BY_ID = PRODUCTS + "/{id}";

    // Admin
    public static final String ADMIN = API + "/admin";
    public static final String ADMIN_CATEGORIES = ADMIN + "/categories";
    public static final String ADMIN_PRODUCTS = ADMIN + "/products";
    public static final String ADMIN_WAREHOUSES = ADMIN + "/warehouses";
    public static final String ADMIN_INVENTORY = ADMIN + "/inventory";
    public static final String ADMIN_DISCOUNTS = ADMIN + "/discounts";

    // Customer
    public static final String CART = API + "/cart";
    public static final String CART_ITEMS = CART + "/items";
    public static final String CHECKOUT = API + "/checkout";
    public static final String ORDERS = API + "/orders";
    public static final String ORDER_BY_ID = ORDERS + "/{id}";
    public static final String ORDER_RETURN = ORDER_BY_ID + "/return";
    /** Security-rule form of {@link #ORDER_RETURN}: any order id. */
    public static final String ORDER_RETURN_PATTERN = ORDERS + "/*/return";

    // Warehouse staff
    public static final String FULFILLMENT = API + "/fulfillment";
    public static final String FULFILLMENT_ORDER_STATUS = FULFILLMENT + "/orders/{id}/status";

    private ApiPaths() {
    }

    /** Ant-style pattern matching a path and everything below it, for security rules. */
    public static String andBelow(String path) {
        return path + "/**";
    }
}
