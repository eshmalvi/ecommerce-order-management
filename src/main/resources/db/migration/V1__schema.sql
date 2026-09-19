-- Invariants live in the database so no application bug can bypass them.

create table category (
    id   bigint generated always as identity primary key,
    name text not null unique
);

create table product (
    id          bigint generated always as identity primary key,
    sku         text          not null unique,
    name        text          not null,
    price       numeric(12,2) not null check (price >= 0),
    category_id bigint        not null references category(id)
);
create index idx_product_category on product(category_id);

create table warehouse (
    id   bigint generated always as identity primary key,
    name text not null unique
);

-- One row per (product, warehouse). quantity can never go negative, whatever the application does.
create table inventory (
    id           bigint generated always as identity primary key,
    product_id   bigint not null references product(id),
    warehouse_id bigint not null references warehouse(id),
    quantity     int    not null check (quantity >= 0),
    unique (product_id, warehouse_id)
);

create table discount (
    code        text         primary key,
    percent_off numeric(5,2) not null check (percent_off > 0 and percent_off <= 100)
);

-- One row per (customer, product); adding the same product again increments the row.
create table cart_item (
    id         bigint generated always as identity primary key,
    customer   text   not null,
    product_id bigint not null references product(id),
    quantity   int    not null check (quantity > 0),
    unique (customer, product_id)
);

-- "order" is a reserved word, hence "orders".
create table orders (
    id              bigint generated always as identity primary key,
    customer        text          not null,
    status          text          not null
                    check (status in ('PLACED', 'CONFIRMED', 'PACKED', 'SHIPPED', 'DELIVERED', 'RETURNED')),
    subtotal        numeric(12,2) not null,
    discount_amount numeric(12,2) not null default 0,
    tax_amount      numeric(12,2) not null,
    total           numeric(12,2) not null,
    discount_code   text          references discount(code),
    payment_ref     text,
    refund_ref      text,
    created_at      timestamptz   not null default now(),
    updated_at      timestamptz   not null default now()
);
create index idx_orders_customer on orders(customer);

-- unit_price is a snapshot of the price paid; warehouse_id lets a return restock the right warehouse.
create table order_line (
    id           bigint generated always as identity primary key,
    order_id     bigint        not null references orders(id) on delete cascade,
    product_id   bigint        not null references product(id),
    warehouse_id bigint        not null references warehouse(id),
    quantity     int           not null check (quantity > 0),
    unit_price   numeric(12,2) not null
);
create index idx_order_line_order on order_line(order_id);

-- type = STATUS for status changes (written in the same transaction),
--        ROUTING / NOTIFICATION / AUDIT for the asynchronous post-checkout pipeline.
create table audit_log (
    id       bigint generated always as identity primary key,
    order_id bigint      not null references orders(id) on delete cascade,
    type     text        not null,
    message  text        not null,
    at       timestamptz not null default now()
);
create index idx_audit_log_order on audit_log(order_id);
