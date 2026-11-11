package ua.kpi.blindpig.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaveDiscountRequest(String name, DiscountType type, BigDecimal value, Long menuItemId, Long categoryId, LocalDateTime validFrom, LocalDateTime validUntil, boolean active) {}
