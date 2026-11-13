package ua.kpi.blindpig.billing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.kpi.blindpig.billing.dto.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Automatic discount targeting exactly one menu item or one category. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Discount {
    private Long id;
    private String name;
    private DiscountType type;
    private BigDecimal value;
    private Long menuItemId;
    private Long categoryId;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private boolean active;
}
