package ua.kpi.blindpig.billing.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequest(
        @Schema(example = "1", description = "Existing order in CREATED status")
        @NotNull(message = "Order id is required")
        @Positive(message = "Order id must be positive")
        Long orderId
) {}
