package ua.kpi.blindpig.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SavePromoCodeRequest(
        @Schema(example = "WELCOME10", description = "3-32 chars: letters, digits, '-' or '_'; stored upper-case")
        @NotBlank(message = "Promo code must not be blank")
        @Pattern(regexp = "^\\s*[A-Za-z0-9_-]{3,32}\\s*$",
                message = "Promo code must be 3-32 characters: letters, digits, '-' or '_'")
        String code,

        @Schema(example = "PERCENTAGE")
        @NotNull(message = "Discount type is required")
        DiscountType type,

        @Schema(example = "10", description = "Percent in (0, 100] for PERCENTAGE, UAH > 0 for FIXED")
        @NotNull(message = "Discount value is required")
        @Positive(message = "Discount value must be greater than 0")
        @DecimalMax(value = "100000", message = "Discount value is too large")
        BigDecimal value,

        @Schema(example = "200", nullable = true, description = "Minimum subtotal after automatic discounts")
        @PositiveOrZero(message = "Minimum order amount must not be negative")
        BigDecimal minOrderAmount,

        @Schema(example = "2026-11-01T00:00:00", nullable = true)
        LocalDateTime validFrom,

        @Schema(example = "2026-12-31T23:59:59", nullable = true)
        LocalDateTime validUntil,

        @Schema(example = "true")
        boolean active
) {}
