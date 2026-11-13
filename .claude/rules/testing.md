---
paths:
  - "src/test/**"
---

# Testing

- Tests live in `src/test/java/ua/kpi/blindpig/<domain>/`. Name them `<Class>Test`, with method names like `shouldRejectDuplicateCode`.
- **Service tests are plain JUnit 5.** Build the `*ServiceImpl` by hand with the real in-memory repository (`new InMemoryXRepository()`), plus **Mockito mocks for other domains' services** (e.g. `OrderService`). Use `Clock.fixed(...)` for time-dependent rules.
- **Controller tests** use `@WebMvcTest(XController.class)` + `MockMvc` + `@MockitoBean` for the services (Spring Boot 4: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`, `org.springframework.test.context.bean.override.mockito.MockitoBean`).
  - REST: status codes, `Location` header, JSON fields (`jsonPath`), and the error body for 400/404/409.
  - Pages: view name, model attributes, redirect + flash after POST, form re-render on error.
- Every service method needs its happy path **and** its negative cases: not found (404), invalid input (400), conflict (409).
- Compare `BigDecimal` with `isEqualByComparingTo` / `compareTo` (scale-insensitive), or assert the exact scale-2 value as a string.
- Don't start the full `@SpringBootTest` context to test domain logic. `BlindPigApplicationTests` stays `@Disabled` until every domain has bean implementations.
- Labs 5–6: repository adapters get integration tests against a real PostgreSQL test DB, including a rollback scenario.
