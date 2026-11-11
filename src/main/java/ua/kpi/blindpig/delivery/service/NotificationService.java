package ua.kpi.blindpig.delivery.service;

import ua.kpi.blindpig.delivery.dto.*;
import java.util.List;

public interface NotificationService {
    /** Persistent in-app notification; orderId may be null. */
    NotificationDto create(Long customerId, Long orderId, String message);
    NotificationDto getById(Long notificationId);
    List<NotificationDto> listForCustomer(Long customerId);
    NotificationDto markRead(Long notificationId);
    void delete(Long notificationId);
}
