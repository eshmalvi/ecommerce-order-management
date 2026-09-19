package com.eish.oms;

/**
 * The identifiers and quantities in {@code V2__seed.sql} / {@code seed.sql}, so tests never repeat them.
 */
public final class SeedData {

    public static final String CATEGORY_ELECTRONICS = "Electronics";
    public static final String CATEGORY_BOOKS = "Books";
    public static final int CATEGORY_COUNT = 2;

    public static final String SKU_HEADPHONES = "SKU-HEADPHONES";
    public static final String SKU_KEYBOARD = "SKU-KEYBOARD";
    public static final String SKU_DDIA = "SKU-DDIA";
    public static final int PRODUCT_COUNT = 3;

    public static final String WAREHOUSE_EAST = "WH-EAST";
    public static final String WAREHOUSE_WEST = "WH-WEST";
    public static final int WAREHOUSE_COUNT = 2;

    /** Headphones are the scarce product used by the concurrency tests: 6 east + 4 west = 10 units. */
    public static final int HEADPHONES_EAST = 6;
    public static final int HEADPHONES_WEST = 4;
    public static final int HEADPHONES_TOTAL = HEADPHONES_EAST + HEADPHONES_WEST;
    public static final int INVENTORY_ROW_COUNT = 5;

    public static final String DISCOUNT_CODE = "SAVE10";
    public static final int DISCOUNT_COUNT = 1;

    private SeedData() {
    }
}
