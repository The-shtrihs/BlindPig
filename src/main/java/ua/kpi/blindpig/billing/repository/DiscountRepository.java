package ua.kpi.blindpig.billing.repository;

import ua.kpi.blindpig.billing.model.Discount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiscountRepository {
    Optional<Discount> findById(Long id);
    List<Discount> findAll();
    Discount save(Discount discount);
    void deleteById(Long id);
    boolean existsById(Long id);

    /** Discounts with active=true and validFrom <= at < validUntil (null bounds are open). */
    List<Discount> findActiveAt(LocalDateTime at);

    /** Active discounts at {@code at} that target the menu item or (if not null) its category. */
    List<Discount> findActiveFor(Long menuItemId, Long categoryId, LocalDateTime at);
}
