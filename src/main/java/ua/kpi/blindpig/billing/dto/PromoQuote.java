package ua.kpi.blindpig.billing.dto;

import java.math.BigDecimal;

public record PromoQuote(Long promoCodeId, String code, BigDecimal discountAmount) {}
