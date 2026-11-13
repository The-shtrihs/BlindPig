package ua.kpi.blindpig.billing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.kpi.blindpig.billing.dto.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Order-level promo code, applied once after automatic discounts. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {
    private Long id;
    private String code;
    private DiscountType type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private boolean active;
}
