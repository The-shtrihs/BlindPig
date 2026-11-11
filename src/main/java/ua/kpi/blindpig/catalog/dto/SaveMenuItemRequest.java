package ua.kpi.blindpig.catalog.dto;

import java.math.BigDecimal;

public record SaveMenuItemRequest(Long categoryId, String name, String description, BigDecimal price, boolean available) {}
