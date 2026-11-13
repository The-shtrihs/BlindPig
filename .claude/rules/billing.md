---
paths:
  - "src/main/java/ua/kpi/blindpig/billing/**"
  - "src/test/java/ua/kpi/blindpig/billing/**"
  - "src/main/resources/templates/billing/**"
---

# Billing domain rules (Payments & Promotions)

Entities are `Discount`, `PromoCode` and `Payment`. Services are `DiscountService`, `PromoCodeService` and `PaymentService`. Outgoing dependency: **only `OrderService`** (from Payment). Billing does not call Catalog.

## Discount (automatic, per unit)
- Target is **exactly one** of `menuItemId` or `categoryId` (XOR); otherwise 400.
- `PERCENTAGE` needs `0 < value ≤ 100`; `FIXED` needs `value > 0` (UAH). The name must not be blank and the type must not be null.
- Active at `at` ⇔ `active && (validFrom == null || validFrom ≤ at) && (validUntil == null || at < validUntil)`. The window is half-open, `[validFrom, validUntil)`. If both dates are set, `validUntil > validFrom` is required.
- `calculateBestDiscount(menuItemId, categoryId, baseUnitPrice, at)` takes the active discounts on that item **or** its category and picks the **single largest** per-unit discount (they never stack; a tie goes to the lower id). `finalUnitPrice = max(0, base − discount)`. With no match it returns `(null, base, 0.00, base)`.

## PromoCode (whole order, after automatic discounts)
- `code` is trimmed and stored upper-case, unique case-insensitively (duplicate → 409).
- `applyPromo(code, subtotalAfterAuto, at)`: blank code or null/negative subtotal → 400. Unknown, inactive, outside the window, or `subtotal < minOrderAmount` → **409**. The discount is capped at the subtotal.
- It is applied once, to the sum **after** automatic discounts.

## Money math (single formula for the team)
```
percentage amount = base × value / 100, rounded HALF_UP to scale 2
fixed amount      = min(value, base)
finalUnitPrice    = base − amount   (never negative)
orderTotal        = max(0, subtotalAfterAuto − promoDiscount)
```
Golden example: 300.00 → item −20% → **240.00** → WELCOME10 −10% → **216.00**. If item 20% and category 15% both apply, use 20%, not 35%.

## Payment
- `createForOrder(orderId)` allows one payment per order (second → 409). The order comes from `OrderService.getById` (404 passes through) and must be `CREATED` (else 409). `amount = order.total()`, status `PENDING`.
- `simulateResult(paymentId, outcome)` only works from `PENDING` (else 409), exactly once.
  - `SUCCESS`: call `orderService.confirmPaidOrder(orderId)` **first**, then save `PAID`. If confirm throws, the payment stays `PENDING`.
  - `FAILURE` → `FAILED`, which is **terminal** (no retry in the MVP).
- Never add a dependency from `order` back to billing's `PaymentService` (that would be a cycle).

## Web
- Pages: `/promotions` (discounts, promo codes, `/promotions/calculator`) and `/payments`.
- REST: `/api/discounts`, `/api/promo-codes`, `/api/payments`, with errors handled by `BillingExceptionHandler`.
