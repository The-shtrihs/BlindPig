package ua.kpi.blindpig.delivery.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.kpi.blindpig.delivery.dto.DeliveryStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    private Long id;
    private Long orderId;
    private Long courierId;
    private DeliveryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;

    public Delivery(Long orderId, Long courierId, DeliveryStatus status, LocalDateTime createdAt, LocalDateTime deliveredAt) {
        this(null, orderId, courierId, status, createdAt, deliveredAt);
    }
}
