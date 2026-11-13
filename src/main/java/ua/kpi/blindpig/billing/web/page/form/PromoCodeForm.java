package ua.kpi.blindpig.billing.web.page.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.billing.dto.PromoCodeDto;
import ua.kpi.blindpig.billing.dto.SavePromoCodeRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Mutable form-backing bean for the promo code HTML form. */
@Data
@NoArgsConstructor
public class PromoCodeForm {

    private String code;
    private DiscountType type = DiscountType.PERCENTAGE;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    @DateTimeFormat(pattern = DiscountForm.DATE_TIME_LOCAL)
    private LocalDateTime validFrom;
    @DateTimeFormat(pattern = DiscountForm.DATE_TIME_LOCAL)
    private LocalDateTime validUntil;
    private boolean active = true;

    public static PromoCodeForm from(PromoCodeDto dto) {
        PromoCodeForm form = new PromoCodeForm();
        form.setCode(dto.code());
        form.setType(dto.type());
        form.setValue(dto.value());
        form.setMinOrderAmount(dto.minOrderAmount());
        form.setValidFrom(dto.validFrom());
        form.setValidUntil(dto.validUntil());
        form.setActive(dto.active());
        return form;
    }

    public SavePromoCodeRequest toRequest() {
        return new SavePromoCodeRequest(code, type, value, minOrderAmount, validFrom, validUntil, active);
    }
}
