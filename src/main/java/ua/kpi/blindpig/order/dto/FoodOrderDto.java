package ua.kpi.blindpig.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FoodOrderDto(Long id, Long customerId, String deliveryAddressSnapshot, OrderStatus status, List<OrderItemDto> items, BigDecimal subtotal, BigDecimal automaticDiscountTotal, BigDecimal promoDiscountTotal, BigDecimal total, LocalDateTime createdAt) {}
