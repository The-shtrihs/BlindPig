package ua.kpi.blindpig.billing.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ua.kpi.blindpig.billing.dto.PaymentOutcome;

public record SimulatePaymentRequest(
        @Schema(example = "SUCCESS")
        @NotNull(message = "Payment outcome is required")
        PaymentOutcome outcome
) {}
