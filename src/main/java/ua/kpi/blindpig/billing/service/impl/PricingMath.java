package ua.kpi.blindpig.billing.service.impl;

import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.common.InvalidRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/** Shared money and validity rules for discounts and promo codes (see docs/DESIGN.md §7). */
final class PricingMath {

    static final int SCALE = 2;
    static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private PricingMath() {
    }

    static BigDecimal money(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING);
    }

    /** Discount amount for {@code base}, rounded HALF_UP to 2 places and never greater than {@code base}. */
    static BigDecimal discountAmount(DiscountType type, BigDecimal value, BigDecimal base) {
        BigDecimal raw = switch (type) {
            case PERCENTAGE -> base.multiply(value).divide(HUNDRED, SCALE, ROUNDING);
            case FIXED -> value;
        };
        return money(raw.min(base));
    }

    static void validateTypeAndValue(DiscountType type, BigDecimal value) {
        if (type == null) {
            throw new InvalidRequestException("Discount type is required");
        }
        if (value == null || value.signum() <= 0) {
            throw new InvalidRequestException("Discount value must be greater than 0");
        }
        if (type == DiscountType.PERCENTAGE && value.compareTo(HUNDRED) > 0) {
            throw new InvalidRequestException("Percentage discount must be in range (0, 100]");
        }
    }

    static void validateWindow(LocalDateTime validFrom, LocalDateTime validUntil) {
        if (validFrom != null && validUntil != null && !validUntil.isAfter(validFrom)) {
            throw new InvalidRequestException("validUntil must be after validFrom");
        }
    }

    /** Half-open window [validFrom, validUntil); a null bound is open. */
    static boolean isActiveAt(boolean active, LocalDateTime validFrom, LocalDateTime validUntil, LocalDateTime at) {
        return active
                && (validFrom == null || !at.isBefore(validFrom))
                && (validUntil == null || at.isBefore(validUntil));
    }

    static void requireNonNegative(BigDecimal amount, String field) {
        if (amount == null) {
            throw new InvalidRequestException(field + " is required");
        }
        if (amount.signum() < 0) {
            throw new InvalidRequestException(field + " must not be negative");
        }
    }
}
