package ua.kpi.blindpig.delivery.service.impl;

import org.springframework.stereotype.Service;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;
import ua.kpi.blindpig.delivery.dto.NotificationDto;
import ua.kpi.blindpig.delivery.model.Notification;
import ua.kpi.blindpig.delivery.repository.NotificationRepository;
import ua.kpi.blindpig.delivery.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationDto create(Long customerId, Long orderId, String message) {
        if (customerId == null) {
            throw new InvalidRequestException("Customer id cannot be null");
        }
        if (message == null || message.isBlank()) {
            throw new InvalidRequestException("Notification message cannot be blank");
        }

        Notification notification = new Notification(
                customerId,
                orderId,
                message.trim(),
                false,
                LocalDateTime.now()
        );
        Notification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    @Override
    public NotificationDto getById(Long notificationId) {
        if (notificationId == null) {
            throw new InvalidRequestException("Notification id cannot be null");
        }
        return notificationRepository.findById(notificationId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
    }

    @Override
    public List<NotificationDto> listForCustomer(Long customerId) {
        if (customerId == null) {
            throw new InvalidRequestException("Customer id cannot be null");
        }
        return notificationRepository.findByCustomerId(customerId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public NotificationDto markRead(Long notificationId) {
        if (notificationId == null) {
            throw new InvalidRequestException("Notification id cannot be null");
        }
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    @Override
    public void delete(Long notificationId) {
        if (notificationId == null) {
            throw new InvalidRequestException("Notification id cannot be null");
        }
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getCustomerId(),
                notification.getOrderId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
