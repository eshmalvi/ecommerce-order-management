-- Demo data. Subselects instead of hard-coded ids so the script never depends on insertion order.

insert into category (name) values ('Electronics'), ('Books');

insert into product (sku, name, price, category_id) values
  ('SKU-HEADPHONES', 'Wireless Headphones', 99.99, (select id from category where name = 'Electronics')),
  ('SKU-KEYBOARD',   'Mechanical Keyboard', 79.50, (select id from category where name = 'Electronics')),
  ('SKU-DDIA',       'Designing Data-Intensive Applications', 45.00, (select id from category where name = 'Books'));

insert into warehouse (name) values ('WH-EAST'), ('WH-WEST');

-- Headphones: 6 + 4 = 10 units across two warehouses, used by the concurrency demo.
insert into inventory (product_id, warehouse_id, quantity) values
  ((select id from product where sku = 'SKU-HEADPHONES'), (select id from warehouse where name = 'WH-EAST'),   6),
  ((select id from product where sku = 'SKU-HEADPHONES'), (select id from warehouse where name = 'WH-WEST'),   4),
  ((select id from product where sku = 'SKU-KEYBOARD'),   (select id from warehouse where name = 'WH-EAST'),  50),
  ((select id from product where sku = 'SKU-KEYBOARD'),   (select id from warehouse where name = 'WH-WEST'),  50),
  ((select id from product where sku = 'SKU-DDIA'),       (select id from warehouse where name = 'WH-EAST'), 100);

insert into discount (code, percent_off) values ('SAVE10', 10.00);
