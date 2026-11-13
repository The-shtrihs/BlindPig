---
paths:
  - "src/**"
---

# Architecture rules (modular monolith)

- **Each domain owns its data.** A domain's `model/` and `repository/` are private to it. Never import `ua.kpi.blindpig.<other>.repository` or `ua.kpi.blindpig.<other>.model`.
- **Cross-domain calls go through service interfaces only** (`ua.kpi.blindpig.<other>.service.*Service`). Pass IDs and contract DTOs, never models or entities.
- **Allowed dependency directions** (contract §2), with no cycles:
  Cart → Customer, Catalog · Order → Cart, Customer, Catalog, Discount, PromoCode · Payment → Order · Delivery → Order, Courier, Notification.
  `OrderService` must never depend on `PaymentService` or `DeliveryService`.
- **A service depends on its own repository interface**, never on `InMemory…`, `Jdbc…`, `Jpa…`, `JdbcTemplate`, `EntityManager`, `CrudRepository` or a raw `Map`.
- **One active bean per repository interface.** When migrating InMemory → JDBC → JPA, replace the adapter. Don't add `@Primary` or profiles.
- In lab 6, Spring Data `CrudRepository` sits behind a thin `Jpa…Repository` adapter that implements the domain interface. Services never see it.
- Service interfaces, DTO records, enums and `common/` exceptions are **frozen team contracts** (`docs/SERVICE_CONTRACTS.md`). Adding Bean Validation annotations to request records is allowed. Changing signatures or fields is not, unless the user says so.
- Services return DTOs, `List<DTO>`, `PageResult<DTO>` or `void`, never models. HTTP types (`ResponseEntity`, `HttpStatus`) never appear in services.

## Don't build
Service factories, generic CRUD services, mapper frameworks, event buses, state-machine libraries, JWT/OAuth, real payment gateways, Redis or brokers, microservices, multi-module Maven. Interfaces are only for repositories and the agreed services, not for every helper.
