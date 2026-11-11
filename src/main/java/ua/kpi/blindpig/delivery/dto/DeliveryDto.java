package ua.kpi.blindpig.delivery.dto;

import java.time.LocalDateTime;

public record DeliveryDto(Long id, Long orderId, Long courierId, DeliveryStatus status, LocalDateTime createdAt, LocalDateTime deliveredAt) {}
