package ua.kpi.blindpig.billing.dto;

import java.math.BigDecimal;

public record DiscountQuote(Long discountId, BigDecimal baseUnitPrice, BigDecimal discountPerUnit, BigDecimal finalUnitPrice) {}
