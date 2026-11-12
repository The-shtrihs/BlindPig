package ua.kpi.blindpig.delivery.repository.inmemory;

import org.springframework.stereotype.Repository;
import ua.kpi.blindpig.delivery.model.Notification;
import ua.kpi.blindpig.delivery.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryNotificationRepository implements NotificationRepository {

    private final Map<Long, Notification> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Optional<Notification> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Notification> findByCustomerId(Long customerId) {
        if (customerId == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(n -> Objects.equals(n.getCustomerId(), customerId))
                .toList();
    }

    @Override
    public List<Notification> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            notification.setId(idSequence.getAndIncrement());
        }
        storage.put(notification.getId(), notification);
        return notification;
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

    public void clear() {
        storage.clear();
    }
}
