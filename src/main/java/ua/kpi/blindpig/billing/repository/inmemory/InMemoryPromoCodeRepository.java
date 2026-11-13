package ua.kpi.blindpig.billing.repository.inmemory;

import org.springframework.stereotype.Repository;
import ua.kpi.blindpig.billing.model.PromoCode;
import ua.kpi.blindpig.billing.repository.PromoCodeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPromoCodeRepository implements PromoCodeRepository {

    private final Map<Long, PromoCode> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Optional<PromoCode> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<PromoCode> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        String wanted = code.trim();
        return storage.values().stream()
                .filter(p -> p.getCode().equalsIgnoreCase(wanted))
                .findFirst();
    }

    @Override
    public List<PromoCode> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(PromoCode::getId))
                .toList();
    }

    @Override
    public PromoCode save(PromoCode promoCode) {
        if (promoCode.getId() == null) {
            promoCode.setId(idSequence.getAndIncrement());
        }
        storage.put(promoCode.getId(), promoCode);
        return promoCode;
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
    public boolean existsByCode(String code) {
        return findByCode(code).isPresent();
    }
}
