package ua.kpi.blindpig.delivery.repository.inmemory;

import org.springframework.stereotype.Repository;
import ua.kpi.blindpig.delivery.model.Delivery;
import ua.kpi.blindpig.delivery.repository.DeliveryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryDeliveryRepository implements DeliveryRepository {

    private final Map<Long, Delivery> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Optional<Delivery> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Delivery> findByOrderId(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(d -> Objects.equals(d.getOrderId(), orderId))
                .findFirst();
    }

    @Override
    public List<Delivery> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Delivery> findByCourierId(Long courierId) {
        if (courierId == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(d -> Objects.equals(d.getCourierId(), courierId))
                .toList();
    }

    @Override
    public Delivery save(Delivery delivery) {
        if (delivery.getId() == null) {
            delivery.setId(idSequence.getAndIncrement());
        }
        storage.put(delivery.getId(), delivery);
        return delivery;
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            storage.remove(id);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && storage.containsKey(id);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        if (orderId == null) {
            return false;
        }
        return storage.values().stream()
                .anyMatch(d -> Objects.equals(d.getOrderId(), orderId));
    }

    @Override
    public boolean existsByCourierId(Long courierId) {
        if (courierId == null) {
            return false;
        }
        return storage.values().stream()
                .anyMatch(d -> Objects.equals(d.getCourierId(), courierId));
    }

    public void clear() {
        storage.clear();
    }
}
