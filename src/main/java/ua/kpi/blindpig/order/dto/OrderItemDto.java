package ua.kpi.blindpig.order.dto;

import java.math.BigDecimal;

public record OrderItemDto(Long menuItemId, String itemNameSnapshot, int quantity, BigDecimal baseUnitPrice, BigDecimal finalUnitPrice) {}
