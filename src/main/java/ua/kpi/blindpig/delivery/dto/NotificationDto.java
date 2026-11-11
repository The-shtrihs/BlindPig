package ua.kpi.blindpig.delivery.dto;

import java.time.LocalDateTime;

public record NotificationDto(Long id, Long customerId, Long orderId, String message, boolean read, LocalDateTime createdAt) {}
