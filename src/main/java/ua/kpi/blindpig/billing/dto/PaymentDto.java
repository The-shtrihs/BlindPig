package ua.kpi.blindpig.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDto(Long id, Long orderId, BigDecimal amount, PaymentStatus status, LocalDateTime createdAt) {}
