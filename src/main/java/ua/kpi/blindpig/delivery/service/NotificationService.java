package ua.kpi.blindpig.delivery.service;

import ua.kpi.blindpig.delivery.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    NotificationDto create(Long customerId, Long orderId, String message);
    NotificationDto getById(Long notificationId);
    List<NotificationDto> listForCustomer(Long customerId);
    NotificationDto markRead(Long notificationId);
    void delete(Long notificationId);
}
