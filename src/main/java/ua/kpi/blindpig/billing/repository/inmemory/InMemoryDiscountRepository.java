package ua.kpi.blindpig.billing.repository.inmemory;

import org.springframework.stereotype.Repository;
import ua.kpi.blindpig.billing.model.Discount;
import ua.kpi.blindpig.billing.repository.DiscountRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryDiscountRepository implements DiscountRepository {

    private final Map<Long, Discount> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Optional<Discount> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Discount> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Discount::getId))
                .toList();
    }

    @Override
    public Discount save(Discount discount) {
        if (discount.getId() == null) {
            discount.setId(idSequence.getAndIncrement());
        }
        storage.put(discount.getId(), discount);
        return discount;
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
    public List<Discount> findActiveAt(LocalDateTime at) {
        return findAll().stream()
                .filter(d -> isActiveAt(d, at))
                .toList();
    }

    @Override
    public List<Discount> findActiveFor(Long menuItemId, Long categoryId, LocalDateTime at) {
        return findActiveAt(at).stream()
                .filter(d -> (d.getMenuItemId() != null && d.getMenuItemId().equals(menuItemId))
                        || (categoryId != null && Objects.equals(d.getCategoryId(), categoryId)))
                .toList();
    }

    private static boolean isActiveAt(Discount d, LocalDateTime at) {
        return d.isActive()
                && (d.getValidFrom() == null || !at.isBefore(d.getValidFrom()))
                && (d.getValidUntil() == null || at.isBefore(d.getValidUntil()));
    }
}
