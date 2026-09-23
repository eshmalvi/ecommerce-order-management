package com.eish.oms.common;

/**
 * The database vocabulary: every table and column name, defined once. SQL statements are built from
 * these constants so that a rename in the schema is a compile-time change everywhere it matters.
 *
 * <p>Only names live here. SQL grammar (select, from, where) stays literal in the statements themselves,
 * where it reads as SQL. See the {@code XxxSql} class next to each repository.
 */
public final class Db {

    private Db() {
    }

    public static final class Category {
        public static final String TABLE = "category";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String COLUMNS = String.join(", ", ID, NAME);

        private Category() {
        }
    }

    public static final class Product {
        public static final String TABLE = "product";
        public static final String ID = "id";
        public static final String SKU = "sku";
        public static final String NAME = "name";
        public static final String PRICE = "price";
        public static final String CATEGORY_ID = "category_id";
        public static final String COLUMNS = String.join(", ", ID, SKU, NAME, PRICE, CATEGORY_ID);

        private Product() {
        }
    }

    public static final class Warehouse {
        public static final String TABLE = "warehouse";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String COLUMNS = String.join(", ", ID, NAME);

        private Warehouse() {
        }
    }

    public static final class Inventory {
        public static final String TABLE = "inventory";
        public static final String ID = "id";
        public static final String PRODUCT_ID = "product_id";
        public static final String WAREHOUSE_ID = "warehouse_id";
        public static final String QUANTITY = "quantity";
        public static final String COLUMNS = String.join(", ", ID, PRODUCT_ID, WAREHOUSE_ID, QUANTITY);

        private Inventory() {
        }
    }

    public static final class Discount {
        public static final String TABLE = "discount";
        public static final String CODE = "code";
        public static final String PERCENT_OFF = "percent_off";
        public static final String COLUMNS = String.join(", ", CODE, PERCENT_OFF);

        private Discount() {
        }
    }

    public static final class CartItem {
        public static final String TABLE = "cart_item";
        public static final String ID = "id";
        public static final String CUSTOMER = "customer";
        public static final String PRODUCT_ID = "product_id";
        public static final String QUANTITY = "quantity";

        private CartItem() {
        }
    }

    /** The table is named {@code orders} because {@code order} is a reserved word in SQL. */
    public static final class Orders {
        public static final String TABLE = "orders";
        public static final String ID = "id";
        public static final String CUSTOMER = "customer";
        public static final String STATUS = "status";
        public static final String SUBTOTAL = "subtotal";
        public static final String DISCOUNT_AMOUNT = "discount_amount";
        public static final String TAX_AMOUNT = "tax_amount";
        public static final String TOTAL = "total";
        public static final String DISCOUNT_CODE = "discount_code";
        public static final String PAYMENT_REF = "payment_ref";
        public static final String REFUND_REF = "refund_ref";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
        public static final String COLUMNS = String.join(", ",
                ID, CUSTOMER, STATUS, SUBTOTAL, DISCOUNT_AMOUNT, TAX_AMOUNT, TOTAL,
                DISCOUNT_CODE, PAYMENT_REF, REFUND_REF, CREATED_AT, UPDATED_AT);

        private Orders() {
        }
    }

    public static final class OrderLine {
        public static final String TABLE = "order_line";
        public static final String ID = "id";
        public static final String ORDER_ID = "order_id";
        public static final String PRODUCT_ID = "product_id";
        public static final String WAREHOUSE_ID = "warehouse_id";
        public static final String QUANTITY = "quantity";
        public static final String UNIT_PRICE = "unit_price";

        private OrderLine() {
        }
    }

    public static final class AuditLog {
        public static final String TABLE = "audit_log";
        public static final String ID = "id";
        public static final String ORDER_ID = "order_id";
        public static final String TYPE = "type";
        public static final String MESSAGE = "message";
        public static final String AT = "at";

        private AuditLog() {
        }
    }
}
