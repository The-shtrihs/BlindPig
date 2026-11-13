package ua.kpi.blindpig.billing.web.page;

import java.time.LocalDateTime;

/** Display status of a discount or promo code at a point in time (drives badges and row colours). */
public enum PromotionStatus {
    ACTIVE, UPCOMING, EXPIRED, DISABLED;

    public static PromotionStatus of(boolean active, LocalDateTime validFrom, LocalDateTime validUntil, LocalDateTime now) {
        if (!active) {
            return DISABLED;
        }
        if (validFrom != null && now.isBefore(validFrom)) {
            return UPCOMING;
        }
        if (validUntil != null && !now.isBefore(validUntil)) {
            return EXPIRED;
        }
        return ACTIVE;
    }

    public String cssClass() {
        return name().toLowerCase();
    }
}
