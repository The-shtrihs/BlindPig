package ua.kpi.blindpig.delivery.repository.inmemory;

import org.springframework.stereotype.Repository;
import ua.kpi.blindpig.delivery.model.Courier;
import ua.kpi.blindpig.delivery.repository.CourierRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCourierRepository implements CourierRepository {

    private final Map<Long, Courier> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Optional<Courier> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Courier> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Courier> findByAvailable(boolean available) {
        return storage.values().stream()
                .filter(courier -> courier.isAvailable() == available)
                .toList();
    }

    @Override
    public Courier save(Courier courier) {
        if (courier.getId() == null) {
            courier.setId(idSequence.getAndIncrement());
        }
        storage.put(courier.getId(), courier);
        return courier;
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
