package ua.kpi.blindpig.billing.service;

import ua.kpi.blindpig.billing.dto.*;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PromoCodeService {
    PromoCodeDto create(SavePromoCodeRequest request);
    PromoCodeDto getById(Long promoCodeId);
    PromoCodeDto getByCode(String code);
    List<PromoCodeDto> list();
    PromoCodeDto update(Long promoCodeId, SavePromoCodeRequest request);
    void delete(Long promoCodeId);
    /** Called only if a nonblank code was entered. Invalid/inactive code -> 409. */
    PromoQuote applyPromo(String code, BigDecimal subtotalAfterAuto, LocalDateTime at);
}
