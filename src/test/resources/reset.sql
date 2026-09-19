-- Wipes every application table so each test starts from the seed data alone.
truncate table audit_log, order_line, orders, cart_item, inventory, discount, product, warehouse, category
    restart identity cascade;
