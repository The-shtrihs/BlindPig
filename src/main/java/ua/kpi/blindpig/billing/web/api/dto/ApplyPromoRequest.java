package ua.kpi.blindpig.billing.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ApplyPromoRequest(
        @Schema(example = "WELCOME10")
        @NotBlank(message = "Promo code must not be blank")
        String code,

        @Schema(example = "240.00", description = "Subtotal after automatic discounts")
        @NotNull(message = "Subtotal is required")
        @PositiveOrZero(message = "Subtotal must not be negative")
        BigDecimal subtotal,

        @Schema(example = "2026-11-13T12:00:00", nullable = true, description = "Defaults to now")
        LocalDateTime at
) {}
