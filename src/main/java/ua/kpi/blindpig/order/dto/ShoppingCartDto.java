package ua.kpi.blindpig.order.dto;

import java.util.List;

public record ShoppingCartDto(Long id, Long customerId, List<CartItemDto> items) {}
