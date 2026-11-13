# The Blind Pig — Claude Code guide

A team project (Peaky Porkers, KPI Spring labs 2–6): a food ordering and delivery system for a single restaurant. It is one **modular monolith** Spring Boot app with five domain packages under `ua.kpi.blindpig`.

## Sources of truth — read before changing behaviour
- `docs/DESIGN.md` covers architecture, the ER model, business rules, lab plan and REST/HTML conventions.
- `docs/SERVICE_CONTRACTS.md` has the **exact** service interfaces, DTO records, enums, exceptions and call semantics. If code and this doc disagree, the doc wins. Signature changes need team agreement (a separate small PR).

## Stack
Spring Boot 4.1.2 · Java 25 · Maven wrapper · Spring MVC + Thymeleaf · springdoc-openapi 3.2.0 · Lombok · JUnit 5 + Mockito.
Persistence by stage: in-memory `Map` repositories (labs 2–4), then JDBC + PostgreSQL (lab 5), then Spring Data JPA (lab 6).

## Commands
```bash
./mvnw test                                   # all tests
./mvnw -Dtest='ua.kpi.blindpig.billing.**' test   # one domain
./mvnw -Dtest=DiscountServiceImplTest test    # one class
./mvnw spring-boot:run                        # app on http://localhost:8080
```
Swagger UI is at `/swagger-ui.html` and the OpenAPI JSON at `/v3/api-docs`.

The full context needs a bean for every injected cross-domain service. Until every domain is merged, `BlindPigApplicationTests` stays `@Disabled`, and domain code is verified with unit tests and `@WebMvcTest` slices.

## Domains and owners
| Package | Domain | Owner | Services |
|---|---|---|---|
| `customer` | Customer & Addresses | Іван | `CustomerService`, `AddressService` |
| `catalog` | Catalog & Menu | Саня | `CategoryService`, `MenuItemService` |
| `order` | Shopping Cart & Orders | Даня | `CartService`, `OrderService` |
| `billing` | Payments & Promotions | Максим | `DiscountService`, `PromoCodeService`, `PaymentService` |
| `delivery` | Delivery & Notifications | Міша | `CourierService`, `DeliveryService`, `NotificationService` |

Only change the domain you were asked to work on. Never edit another domain's package, `common/`, or the contract docs unless the user explicitly asks.

## Package layout inside a domain
```
<domain>/
  dto/                 contract records + enums (shared, frozen)
  service/             contract interfaces (shared, frozen)
  service/impl/        @Service implementations
  model/               domain classes (Lombok; become @Entity only in lab 6)
  repository/          repository interfaces (the swap point)
  repository/inmemory/ @Repository adapters for labs 2–4
  web/page/            @Controller + Thymeleaf form beans
  web/api/             @RestController, web-only DTOs, scoped @RestControllerAdvice
  config/, demo/       @Bean config, lab-2 DI demo beans, seed data
templates/<domain>/    Thymeleaf views
```

## Lab stages
| Tag | Adds |
|---|---|
| `lab-2` | repository interfaces + in-memory adapters, services, DI demo (constructor/setter/field, `@Bean`, singleton/prototype) |
| `lab-3` | Thymeleaf pages, forms, conditional formatting, `switch/case`, loops, XSS-safe output |
| `lab-4` | REST CRUD, validation, HTTP statuses, OpenAPI, Postman; Catalog shows JSON Patch + Merge Patch and pagination |
| `lab-5` | JDBC adapters (`JdbcTemplate`/`JdbcClient`), PostgreSQL schema, `@Transactional` + rollback |
| `lab-6` | JPA entities, `CrudRepository` behind a thin adapter, JPQL `@Query`, `@NamedQuery`, derived queries |

Only one repository adapter is active per interface. The old one is replaced, and the previous stage lives in its git tag.

## Git
- Branches use `feature/<domain>` or `feature/<domain>-<topic>`; PRs go into `main`.
- Commit format is `feat(<domain>): [labN] short description` (or `fix`, `docs`, `chore`, `test`).
- **Claude does not commit, push or create tags.** It leaves that to the person and suggests a commit message at logical checkpoints.

## Detailed rules
Path-scoped rules in `.claude/rules/` load automatically:
`architecture.md`, `java-conventions.md`, `web.md`, `testing.md`, and per-domain files such as `billing.md`.

## Hooks (`.claude/settings.json`)
Hooks need `jq`; without it they do nothing.
| Hook | When | Effect |
|---|---|---|
| `protect-contracts.sh` | before Edit/Write | Asks for confirmation before touching `docs/DESIGN.md`, `docs/SERVICE_CONTRACTS.md`, `*/service/*Service.java`, `*/dto/*`, `common/*` |
| `guard-architecture.sh` | after editing `.java` | Blocks imports of another domain's `repository`/`model`; flags `double`/`float` money in `billing` |
| `guard-thymeleaf.sh` | after editing templates | Blocks `th:utext` (XSS) |
| `run-tests-on-stop.sh` | end of turn | If `.java` files changed, runs `./mvnw -q test` and sends failures back to Claude |

To skip the test hook for a session, set `BLINDPIG_SKIP_STOP_TESTS=1` in the environment before starting Claude.
