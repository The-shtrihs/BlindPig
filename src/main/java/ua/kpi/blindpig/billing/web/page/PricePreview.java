package ua.kpi.blindpig.billing.web.page;

import ua.kpi.blindpig.billing.dto.DiscountQuote;

import java.math.BigDecimal;

/**
 * Step-by-step price breakdown shown on the calculator page.
 * Mirrors the checkout formula from docs/SERVICE_CONTRACTS.md §6.3; the real checkout is done by OrderService.
 */
public record PricePreview(
        DiscountQuote quote,
        int quantity,
        BigDecimal subtotal,
        BigDecimal automaticDiscount,
        BigDecimal subtotalAfterAuto,
        String promoCode,
        BigDecimal promoDiscount,
        String promoError,
        BigDecimal total
) {
}
