package ua.kpi.blindpig.delivery.repository;

import ua.kpi.blindpig.delivery.model.Courier;

import java.util.List;
import java.util.Optional;

public interface CourierRepository {
    Optional<Courier> findById(Long id);
    List<Courier> findAll();
    List<Courier> findByAvailable(boolean available);
    Courier save(Courier courier);
    void deleteById(Long id);
    boolean existsById(Long id);
}
