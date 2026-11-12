package ua.kpi.blindpig.delivery.repository;

import ua.kpi.blindpig.delivery.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Optional<Notification> findById(Long id);
    List<Notification> findByCustomerId(Long customerId);
    List<Notification> findAll();
    Notification save(Notification notification);
    void deleteById(Long id);
    boolean existsById(Long id);
}
