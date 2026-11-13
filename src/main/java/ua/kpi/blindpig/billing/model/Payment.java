package ua.kpi.blindpig.billing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.kpi.blindpig.billing.dto.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Demo payment; one per order. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}
