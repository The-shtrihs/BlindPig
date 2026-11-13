package ua.kpi.blindpig.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Exactly one of menuItemId / categoryId must be set; checked by DiscountService (cross-field rule). */
public record SaveDiscountRequest(
        @Schema(example = "Піца тижня −20%")
        @NotBlank(message = "Discount name must not be blank")
        @Size(max = 100, message = "Discount name must be at most 100 characters")
        String name,

        @Schema(example = "PERCENTAGE")
        @NotNull(message = "Discount type is required")
        DiscountType type,

        @Schema(example = "20", description = "Percent in (0, 100] for PERCENTAGE, UAH > 0 for FIXED")
        @NotNull(message = "Discount value is required")
        @Positive(message = "Discount value must be greater than 0")
        @DecimalMax(value = "100000", message = "Discount value is too large")
        BigDecimal value,

        @Schema(example = "1", nullable = true)
        Long menuItemId,

        @Schema(description = "Set instead of menuItemId to target a whole category", nullable = true)
        Long categoryId,

        @Schema(example = "2026-11-01T00:00:00", nullable = true)
        LocalDateTime validFrom,

        @Schema(example = "2026-12-31T23:59:59", nullable = true)
        LocalDateTime validUntil,

        @Schema(example = "true")
        boolean active
) {}
