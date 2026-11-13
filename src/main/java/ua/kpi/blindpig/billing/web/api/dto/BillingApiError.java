package ua.kpi.blindpig.billing.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** Error body; same JSON shape as the delivery module's ApiErrorResponse (candidate for common/). */
@Schema(description = "Error response")
public record BillingApiError(
        @Schema(example = "409") int status,
        @Schema(example = "Conflict") String error,
        @Schema(example = "Promo code WELCOME10 already exists") String message,
        List<String> details,
        LocalDateTime timestamp
) {
    public static BillingApiError of(int status, String error, String message, List<String> details) {
        return new BillingApiError(status, error, message, details, LocalDateTime.now());
    }
}
