package ua.kpi.blindpig.billing.repository;

import ua.kpi.blindpig.billing.model.PromoCode;

import java.util.List;
import java.util.Optional;

public interface PromoCodeRepository {
    Optional<PromoCode> findById(Long id);
    /** Case-insensitive lookup. */
    Optional<PromoCode> findByCode(String code);
    List<PromoCode> findAll();
    PromoCode save(PromoCode promoCode);
    void deleteById(Long id);
    boolean existsById(Long id);
    /** Case-insensitive check. */
    boolean existsByCode(String code);
}
