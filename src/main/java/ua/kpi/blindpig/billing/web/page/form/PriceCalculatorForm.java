package ua.kpi.blindpig.billing.web.page.form;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Inputs of the promotions price calculator page. */
@Data
@NoArgsConstructor
public class PriceCalculatorForm {
    private Long menuItemId = 1L;
    private Long categoryId = 1L;
    private BigDecimal basePrice = new BigDecimal("300.00");
    private int quantity = 1;
    private String promoCode = "WELCOME10";
}
