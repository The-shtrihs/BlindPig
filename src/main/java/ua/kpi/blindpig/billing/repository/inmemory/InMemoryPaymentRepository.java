package ua.kpi.blindpig.billing.repository.inmemory;

import org.springframework.stereotype.Repository;
import ua.kpi.blindpig.billing.dto.PaymentStatus;
import ua.kpi.blindpig.billing.model.Payment;
import ua.kpi.blindpig.billing.repository.PaymentRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<Long, Payment> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Optional<Payment> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(p -> orderId.equals(p.getOrderId()))
                .findFirst();
    }

    @Override
    public List<Payment> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Payment::getId))
                .toList();
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return findAll().stream()
                .filter(p -> p.getStatus() == status)
                .toList();
    }

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            payment.setId(idSequence.getAndIncrement());
        }
        storage.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return findByOrderId(orderId).isPresent();
    }
}
