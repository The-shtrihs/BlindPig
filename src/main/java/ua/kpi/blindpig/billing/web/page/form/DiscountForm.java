package ua.kpi.blindpig.billing.web.page.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ua.kpi.blindpig.billing.dto.DiscountDto;
import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.billing.dto.SaveDiscountRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Mutable form-backing bean for the discount HTML form (records do not work with th:field). */
@Data
@NoArgsConstructor
public class DiscountForm {

    public static final String DATE_TIME_LOCAL = "yyyy-MM-dd'T'HH:mm";

    private String name;
    private DiscountType type = DiscountType.PERCENTAGE;
    private BigDecimal value;
    private Long menuItemId;
    private Long categoryId;
    @DateTimeFormat(pattern = DATE_TIME_LOCAL)
    private LocalDateTime validFrom;
    @DateTimeFormat(pattern = DATE_TIME_LOCAL)
    private LocalDateTime validUntil;
    private boolean active = true;

    public static DiscountForm from(DiscountDto dto) {
        DiscountForm form = new DiscountForm();
        form.setName(dto.name());
        form.setType(dto.type());
        form.setValue(dto.value());
        form.setMenuItemId(dto.menuItemId());
        form.setCategoryId(dto.categoryId());
        form.setValidFrom(dto.validFrom());
        form.setValidUntil(dto.validUntil());
        form.setActive(dto.active());
        return form;
    }

    public SaveDiscountRequest toRequest() {
        return new SaveDiscountRequest(name, type, value, menuItemId, categoryId, validFrom, validUntil, active);
    }
}
