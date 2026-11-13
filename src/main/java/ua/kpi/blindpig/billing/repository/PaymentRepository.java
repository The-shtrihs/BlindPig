package ua.kpi.blindpig.billing.repository;

import ua.kpi.blindpig.billing.dto.PaymentStatus;
import ua.kpi.blindpig.billing.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(Long id);
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findAll();
    List<Payment> findByStatus(PaymentStatus status);
    Payment save(Payment payment);
    boolean existsByOrderId(Long orderId);
}
