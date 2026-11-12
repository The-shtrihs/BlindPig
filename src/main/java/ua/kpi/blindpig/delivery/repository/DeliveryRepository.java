package ua.kpi.blindpig.delivery.repository;

import ua.kpi.blindpig.delivery.model.Delivery;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository {
    Optional<Delivery> findById(Long id);
    Optional<Delivery> findByOrderId(Long orderId);
    List<Delivery> findAll();
    List<Delivery> findByCourierId(Long courierId);
    Delivery save(Delivery delivery);
    void deleteById(Long id);
    boolean existsById(Long id);
    boolean existsByOrderId(Long orderId);
    boolean existsByCourierId(Long courierId);
}
