package ua.kpi.blindpig.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromoCodeDto(Long id, String code, DiscountType type, BigDecimal value, BigDecimal minOrderAmount, LocalDateTime validFrom, LocalDateTime validUntil, boolean active) {}
