package ua.kpi.blindpig.billing.service;

import ua.kpi.blindpig.billing.dto.*;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DiscountService {
    DiscountDto create(SaveDiscountRequest request);
    DiscountDto getById(Long discountId);
    List<DiscountDto> list();
    List<DiscountDto> findActive(LocalDateTime at);
    DiscountDto update(Long discountId, SaveDiscountRequest request);
    void delete(Long discountId);
    /** Best single item/category automatic discount, per UNIT, never below zero. */
    DiscountQuote calculateBestDiscount(Long menuItemId, Long categoryId,
        BigDecimal baseUnitPrice, LocalDateTime at);
}
