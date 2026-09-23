package com.eish.oms.common;

/**
 * Names of the named parameters used in SQL statements. Each name is used twice: as {@code :name} inside the
 * statement (via {@link #bind(String)}) and as the key in {@code .param(name, value)} when the repository
 * binds it. Sharing one constant for both sides means they cannot drift apart.
 */
public final class Params {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SKU = "sku";
    public static final String PRICE = "price";
    public static final String CATEGORY_ID = "categoryId";
    public static final String PRODUCT_ID = "productId";
    public static final String WAREHOUSE_ID = "warehouseId";
    public static final String QUANTITY = "quantity";
    public static final String CODE = "code";
    public static final String PERCENT_OFF = "percentOff";
    public static final String CUSTOMER = "customer";
    public static final String ORDER_ID = "orderId";
    public static final String STATUS = "status";
    public static final String SUBTOTAL = "subtotal";
    public static final String DISCOUNT_AMOUNT = "discountAmount";
    public static final String TAX_AMOUNT = "taxAmount";
    public static final String TOTAL = "total";
    public static final String DISCOUNT_CODE = "discountCode";
    public static final String PAYMENT_REF = "paymentRef";
    public static final String REFUND_REF = "refundRef";
    public static final String UNIT_PRICE = "unitPrice";
    public static final String TYPE = "type";
    public static final String MESSAGE = "message";

    private Params() {
    }

    /** The placeholder form of a parameter as it appears in SQL, for example {@code :productId}. */
    public static String bind(String name) {
        return ":" + name;
    }
}
