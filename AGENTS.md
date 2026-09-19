# AGENTS.md

Instructions for the AI coding agent working on this repository. Written before the first line of
application code and kept up to date as the work progresses. The human reviews every commit and
performs every push.

## Purpose

A Spring Boot order management service for an e-commerce shop: catalog, cart, checkout, inventory across
warehouses, payment, fulfillment lifecycle, returns and refunds, with role-based access for admin,
customer and warehouse staff. The three requirements that matter most and must be provably correct:

1. Stock is never oversold under concurrent checkouts, across warehouses.
2. Checkout atomically reflects cart, inventory and payment state.
3. Fulfillment routing, customer notification and audit logging run after checkout without blocking it.

The full plan is in `docs/plan.md`; the assignment text is in `docs/brief.md`.

## Hard constraints

- Java 21, Spring Boot 4.1.x pinned in `pom.xml`. Maven wrapper only; the reviewer must not need Maven.
- PostgreSQL only. Embedded (Zonky) under the `local` profile so nothing needs installing; `postgres`
  profile for an external database. Never switch to H2.
- No Docker, no message brokers, no caches, no UI, no JPA/Hibernate, no Lombok, no Swagger, no Actuator.
- Data access is `JdbcClient` with hand-written SQL in text blocks. Rows and DTOs are Java records.
- Money is `BigDecimal` (scale 2, HALF_UP) in Java and `numeric(12,2)` in the database. Never `double`.
- Invariants live in the database: unique constraints, check constraints, foreign keys.
- No network I/O inside a database transaction. The payment gateway is in-process, which is the only
  reason checkout is a single transaction; document this wherever it matters.
- Stock decrement is one atomic conditional `UPDATE ... WHERE quantity >= :q`. Never read-check-write.
  Never `FOR UPDATE SKIP LOCKED` for inventory.
- Post-checkout work uses `@Async @TransactionalEventListener(AFTER_COMMIT)` on a bounded executor.
- Every commit compiles and passes `./mvnw -q verify`. Every feature commit includes its tests.
- Never run `git push`. Commit locally; the human pushes.

## Conventions

- Package by feature: `catalog`, `inventory`, `discount`, `cart`, `pricing`, `payment`, `order`,
  `pipeline`, plus `config` and `common`.
- Controllers translate HTTP to service calls and back, nothing else. Services own use cases and
  transaction boundaries. Repositories own data access, one class per table.
- No magic strings. Anything the system depends on as an identifier is a named constant: SQL statements
  in a package-private `XxxSql` class next to each repository (`ProductSql.FIND_BY_ID`); URL paths in
  `config/ApiPaths` (used by both controllers and `SecurityConfig`, so they cannot drift); roles in
  `config/Roles`; profile names in `config/Profiles`; demo credentials in `config/DemoUsers`; seed
  identifiers for tests in `SeedData`. What stays inline: one-off human-readable message text, SQL parameter
  names (they belong to the statement that declares them), validation annotation values, and test inputs
  such as request JSON.
- One `@RestControllerAdvice` returns RFC 7807 `ProblemDetail` for every error. Status codes:
  400 validation, 401 no credentials, 402 payment declined, 403 wrong role, 404 not found or not yours,
  409 conflict with current state (duplicate, insufficient stock, illegal transition, empty cart).
- Plain, self-explanatory names. Short Javadoc on every public class and public method.
- Design patterns only where they arise naturally (payment port and adapter, allocation strategy,
  state machine as data). No speculative abstractions.
- Conventional Commits, subject at most 50 characters, imperative mood, body explains what and why.
- Tests: unit tests for pure logic, `@SpringBootTest` integration tests against embedded PostgreSQL,
  Awaitility for asynchronous assertions, never `Thread.sleep` in real tests.
  Tests tagged `demo` show bugs on purpose and are excluded from `./mvnw verify`.

## How to run

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS; any JDK 21 works
./mvnw spring-boot:run          # starts embedded PostgreSQL, migrates, seeds, listens on :8080
./mvnw -q verify                # full test suite
./mvnw test -Pdemo              # the tagged demo tests that show concurrency bugs
```

Users: `admin/admin123`, `customer/customer123`, `staff/staff123` (HTTP Basic).

## Commit plan and progress

One row per commit. Status is updated by the agent when the commit lands.

| # | Commit | Status |
|---|---|---|
| 1 | chore: Initialise repository | done |
| 2 | chore: Bootstrap project with embedded PostgreSQL | done |
| 3 | feat: Add database schema and seed data | done |
| 4 | feat: Add Basic auth with three roles | done |
| 5 | feat: Add catalog with categories and products | done |
| 6 | feat: Add warehouses and inventory management | done |
| 7 | feat: Add discount codes | |
| 8 | feat: Add pricing with discount and tax | |
| 9 | feat: Add customer cart | |
| 10 | feat: Add atomic inventory allocation | |
| 11 | feat: Add payment gateway port with fake | |
| 12 | feat: Add order state machine | |
| 13 | feat: Add checkout as a single transaction | |
| 14 | test: Add demo showing naive inventory oversells | |
| 15 | feat: Add fulfillment status updates for staff | |
| 16 | feat: Add returns with refund and restock | |
| 17 | feat: Add async post-checkout pipeline | |
| 18 | docs: Add README with design decisions | |
| 19 | docs: Add raw artifacts and skills note | |

## Notes and gotchas

- Spring Initializr no longer offers Boot 3.x, so the project is on Boot 4.1.1 (Spring Framework 7,
  Spring Security 7, Jackson 3). Starters are the Boot 4 names (`spring-boot-starter-webmvc`, per-module
  `-test` starters).
- Apple Silicon needs the `embedded-postgres-binaries-darwin-arm64v8` artifact; Intel macOS, Linux and
  Windows binaries ship with `embedded-postgres` itself.
- The human stopped the agent once for writing an entire task's worth of code before the first commit;
  the work was re-planned into the small commits listed above.
- Review of commit 5 asked for SQL and shared literals to move into constants classes, then for a
  sweep of the whole codebase (embedded database credentials, profile names, URL paths, seed data in
  tests). Done as one `refactor` commit so the review is visible in the history.
