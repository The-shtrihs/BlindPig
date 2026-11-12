package ua.kpi.blindpig.delivery.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long id;
    private Long customerId;
    private Long orderId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public Notification(Long customerId, Long orderId, String message, boolean read, LocalDateTime createdAt) {
        this(null, customerId, orderId, message, read, createdAt);
    }
}
