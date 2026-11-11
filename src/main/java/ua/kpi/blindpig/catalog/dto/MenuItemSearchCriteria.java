package ua.kpi.blindpig.catalog.dto;

import java.math.BigDecimal;

public record MenuItemSearchCriteria(String name, Long categoryId, Boolean available, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {}
