# Implementation plan

The plan agreed before any code was written. Produced in a planning session with an AI agent (Kiro with
Claude), then handed to the same agent in build mode to implement one commit at a time.

## 1. Scoping decision

Three briefs were offered (e-commerce order management, food delivery, multi-tenant notification service).
E-commerce was chosen for the clearest concurrency story: many writers competing for scarce stock rows,
and a hard line between what belongs inside the checkout transaction and what runs after it.

The available build time was about five hours, so the plan goes deep on the three requirements that
differentiate the submission (no oversell under concurrency, atomic checkout, non-blocking pipeline) and
keeps everything else present but minimal. Every cut is a documented scoping decision, not an omission.

## 2. Functional requirements

| ID | Requirement | Actor |
|---|---|---|
| FR1 | Create categories and products; browse products, filter by category | Admin, Customer |
| FR2 | Create warehouses; set stock per (product, warehouse) | Admin |
| FR3 | Create percentage discount codes | Admin |
| FR4 | Add items to cart (repeat add increments); view cart with price breakdown | Customer |
| FR5 | Checkout: allocate stock across warehouses, apply discount and tax, charge payment, create order, clear cart, all-or-nothing | Customer |
| FR6 | Reject checkout on insufficient stock or declined payment, leaving all state unchanged | System |
| FR7 | Advance order PLACED → CONFIRMED → PACKED → SHIPPED → DELIVERED; reject illegal transitions | Staff |
| FR8 | Return a delivered order: full refund, restock to origin warehouse, status RETURNED | Customer |
| FR9 | List and view own orders with status and history | Customer |
| FR10 | After checkout commits, run fulfillment routing, customer notification and audit logging off the request path | System |
| FR11 | Role-based access for admin, customer, warehouse staff | System |

## 3. Non-functional requirements

| Category | Requirement | How it is met |
|---|---|---|
| Correctness | Stock never negative under any interleaving, across warehouses | Atomic conditional UPDATE plus a database check constraint |
| Atomicity | Checkout is all-or-nothing across cart, inventory, payment, order | Single database transaction |
| Responsiveness | Checkout latency independent of downstream work | After-commit async listeners on a bounded pool |
| Consistency | Money exact to the cent; invariants cannot be bypassed by application bugs | `numeric(12,2)` + `BigDecimal`; unique and check constraints |
| Security | No cross-customer data access; role boundaries enforced | Path-based rules plus service-layer ownership check |
| Testability | Concurrency proven deterministically against the real engine | Embedded PostgreSQL, 50-thread tests, Awaitility |
| Operability | Runs with one command and no infrastructure | Java only; embedded database |
| Non-goals | Horizontal scale, HA, observability, real PSP, UI, brokers | Out of scope per the brief |

## 4. Assumptions

1. Single JVM, single embedded PostgreSQL, no brokers.
2. Three in-memory users over HTTP Basic (`admin`, `customer`, `staff`); the username is the customer identity.
3. Payment is an in-process fake using test card numbers (a card ending `0002` is declined). Because it is
   instant and local, checkout is one transaction. With a real payment provider the design would split into
   reserve → charge → confirm with a TTL sweeper, so no database lock is held across a network call.
4. Inventory is a quantity per (product, warehouse). "Reserved correctly" means the decrement is atomic and
   the quantity never goes negative.
5. One warehouse per order line, no split shipments.
6. One percentage discount per order; flat tax rate on the post-discount subtotal; no shipping fee.
7. PLACED and CONFIRMED happen in the same transaction because payment is synchronous; both are recorded.
8. Returns are whole-order, from DELIVERED, auto-approved, full refund, restock to origin warehouse.
9. Any staff user may update any order.
10. Pipeline events are in-JVM and lost on crash; a transactional outbox is the production fix.
11. Failed checkouts are not persisted; the transaction rolls back and the client gets 402 or 409.
12. Admin endpoints are create-only (plus inventory upsert).
13. Product browsing is public; everything else requires authentication.

## 5. Stack

Java 21, Spring Boot 4.1.x (pinned), Maven wrapper, Spring Web MVC, Spring JDBC (`JdbcClient`, hand-written
SQL, records as rows), Spring Security (HTTP Basic, in-memory users, BCrypt), Bean Validation, Flyway,
PostgreSQL 17 embedded via Zonky for the `local` profile, JUnit 5 + MockMvc + Awaitility.

Not used, on purpose: JPA/Hibernate, Lombok, Docker, Testcontainers, H2, brokers, Swagger, Actuator.

## 6. Data model

Nine tables: `category`, `product`, `warehouse`, `inventory` (unique on product + warehouse, `quantity >= 0`),
`discount`, `cart_item` (unique on customer + product), `orders`, `order_line` (price snapshot and origin
warehouse), `audit_log`. Full DDL in `src/main/resources/db/migration/V1__schema.sql`.

## 7. API

| Method | Path | Role |
|---|---|---|
| POST | `/api/admin/categories`, `/api/admin/products`, `/api/admin/warehouses`, `/api/admin/discounts` | admin |
| PUT | `/api/admin/inventory` | admin |
| GET | `/api/products?categoryId=`, `/api/categories` | public |
| POST | `/api/cart/items` | customer |
| GET | `/api/cart` | customer |
| POST | `/api/checkout` | customer |
| GET | `/api/orders`, `/api/orders/{id}` | customer (own), staff, admin |
| POST | `/api/orders/{id}/return` | customer |
| PATCH | `/api/fulfillment/orders/{id}/status` | staff |

All errors are RFC 7807 `ProblemDetail`.

## 8. Component design

- **Inventory allocation.** For each order line, read candidate warehouses with enough stock ordered by
  quantity descending, then run `UPDATE inventory SET quantity = quantity - :q WHERE product_id = :p AND
  warehouse_id = :w AND quantity >= :q` against each until one returns a row. Zero rows everywhere means
  insufficient stock. Cart lines are processed in product id order so lock acquisition is totally ordered.
- **Checkout.** One `@Transactional` method: load cart, allocate, price, insert order as PLACED, charge,
  update to CONFIRMED, clear cart, publish `OrderPlacedEvent`. Any failure rolls everything back.
- **Payment.** `PaymentGateway` interface with an in-process fake. A real adapter would pass the order id as
  the provider's idempotency key.
- **Lifecycle.** `OrderStatus` enum plus a transition map validated in one place. Security decides who may
  call; the state machine decides whether the move is legal.
- **Returns.** Compensating action: refund through the gateway, restock each line to its warehouse, set
  RETURNED. A second return finds RETURNED and is rejected, so the refund is idempotent.
- **Pipeline.** Three listeners annotated `@Async @TransactionalEventListener(AFTER_COMMIT)`, each writing an
  `audit_log` row (ROUTING, NOTIFICATION, AUDIT). AFTER_COMMIT guarantees a rolled-back order never triggers them.
- **Errors.** One `@RestControllerAdvice`; 400/401/402/403/404/409 as listed in `AGENTS.md`.

## 9. Testing

Unit tests for pure logic (pricing, state machine). `@SpringBootTest` integration tests through MockMvc
against embedded PostgreSQL for every flow. Concurrency tests call the service layer from 50 threads with a
`CountDownLatch` and assert exactly 10 successes against 10 units split across two warehouses. Demo tests
tagged `demo` show the bug in the naive read-check-write approach; they are excluded from `verify` and run
with `./mvnw test -Pdemo`.

## 10. Commit plan

Small commits, each compiling and green:

1. Initialise repository
2. Bootstrap project with embedded PostgreSQL
3. Database schema and seed data
4. Basic auth with three roles
5. Catalog with categories and products (plus error handling)
6. Warehouses and inventory management
7. Discount codes
8. Pricing with discount and tax
9. Customer cart
10. Atomic inventory allocation (with the 50-thread proof)
11. Payment gateway port with fake
12. Order state machine
13. Checkout as a single transaction
14. Demo test showing naive inventory oversells
15. Fulfillment status updates for staff
16. Returns with refund and restock
17. Async post-checkout pipeline
18. README with design decisions
19. Raw artifacts and skills note

Stretch, if time remains: idempotency key on checkout, phantom-event demo, product price update.

## 11. Deliberately out of scope

Idempotency key (unique constraint plus catch-and-return), reservation with TTL (two transactions plus a
sweeper), transactional outbox (event table plus poller), split shipments (greedy allocation across rows),
partial returns (return lines), pagination, admin update/delete, warehouse-scoped staff.
