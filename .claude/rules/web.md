---
paths:
  - "src/main/java/**/web/**"
  - "src/main/resources/templates/**"
  - "src/main/resources/static/**"
---

# Web layer (Thymeleaf + REST)

Both `web/page` (`@Controller`) and `web/api` (`@RestController`) call the **same service**. Controllers hold no business rules.

## REST (`/api/...`)
- `GET` reads, `POST` creates, `PUT` replaces, `PATCH` partially updates, `DELETE` deletes.
- `POST` that creates returns `201 Created` plus a `Location` header (`ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")`). Reads and updates return `200`, and `DELETE` returns `204 No Content`.
- Errors map as `400` for invalid input or unparseable JSON/enum, `404` for not found and `409` for business conflicts.
- Request bodies use `@Valid` with a request record. Responses are DTO records, never models.
- Each domain has one `@RestControllerAdvice(basePackages = "ua.kpi.blindpig.<domain>.web.api")`, scoped to `web.api` so page controllers keep HTML error handling. Error JSON: `{status, error, message, details[], timestamp}`.
- Every endpoint gets OpenAPI docs: `@Tag` on the class, `@Operation(summary)`, `@ApiResponses` listing the possible codes (including 400/404/409), and `@Schema(example=…)` on request fields where it helps.
- Never trust prices or totals sent by the client. The server always recalculates.

## Thymeleaf
- Templates live in `templates/<domain>/`. Use one fragment file per domain for head and nav until a shared layout exists.
- Output goes through `th:text` only. **Never `th:utext`**, because user input must be escaped (a hook enforces this).
- Show the lab-3 features: `th:each`, `th:if`/`th:unless`, Elvis `?:`, `th:switch`/`th:case`, `th:classappend` for conditional formatting.
- Forms bind to **mutable form-backing classes** (`web/page/form/*Form`, Lombok `@Data`), not records. Use `th:object` and `th:field`, and `@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")` for `datetime-local` inputs (ISO `DATE_TIME` expects seconds and an offset, which the browser doesn't send).
- Use POST-Redirect-GET: after a successful POST, redirect and show a flash message (`RedirectAttributes.addFlashAttribute`). On a service error, re-render the form with `bindingResult.reject(...)` or a field error.
- HTML forms have no PUT or DELETE. Use `POST /…/{id}` for update and `POST /…/{id}/delete` for delete.
