package ua.kpi.blindpig.billing;

import org.junit.jupiter.api.Test;
import ua.kpi.blindpig.billing.dto.DiscountQuote;
import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.billing.dto.SaveDiscountRequest;
import ua.kpi.blindpig.billing.dto.SavePromoCodeRequest;
import ua.kpi.blindpig.billing.repository.inmemory.InMemoryDiscountRepository;
import ua.kpi.blindpig.billing.repository.inmemory.InMemoryPromoCodeRepository;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.billing.service.PromoCodeService;
import ua.kpi.blindpig.billing.service.impl.DiscountServiceImpl;
import ua.kpi.blindpig.billing.service.impl.PromoCodeServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** End-to-end pricing as OrderService will use it (docs/DESIGN.md §7.3, SERVICE_CONTRACTS.md §6.3). */
class PricingScenarioTest {

    private static final LocalDateTime CHECKOUT_AT = LocalDateTime.of(2026, 11, 13, 19, 30);

    private final DiscountService discounts = new DiscountServiceImpl(new InMemoryDiscountRepository());
    private final PromoCodeService promos = new PromoCodeServiceImpl(new InMemoryPromoCodeRepository());

    @Test
    void pizza300WithItem20AndCategory15AndWelcome10Gives216() {
        discounts.create(new SaveDiscountRequest("Pizza -20%", DiscountType.PERCENTAGE, new BigDecimal("20"), 1L, null, null, null, true));
        discounts.create(new SaveDiscountRequest("Pizzas -15%", DiscountType.PERCENTAGE, new BigDecimal("15"), null, 2L, null, null, true));
        promos.create(new SavePromoCodeRequest("WELCOME10", DiscountType.PERCENTAGE, new BigDecimal("10"), null, null, null, true));

        BigDecimal base = new BigDecimal("300.00");
        int quantity = 1;
        DiscountQuote quote = discounts.calculateBestDiscount(1L, 2L, base, CHECKOUT_AT);

        BigDecimal subtotal = base.multiply(BigDecimal.valueOf(quantity));
        BigDecimal automaticDiscount = quote.discountPerUnit().multiply(BigDecimal.valueOf(quantity));
        BigDecimal subtotalAfterAuto = subtotal.subtract(automaticDiscount);
        BigDecimal promoDiscount = promos.applyPromo("WELCOME10", subtotalAfterAuto, CHECKOUT_AT).discountAmount();
        BigDecimal total = subtotalAfterAuto.subtract(promoDiscount).max(BigDecimal.ZERO);

        assertThat(quote.finalUnitPrice()).isEqualByComparingTo("240.00");   // 20% wins over 15%, not 35%
        assertThat(subtotalAfterAuto).isEqualByComparingTo("240.00");
        assertThat(promoDiscount).isEqualByComparingTo("24.00");
        assertThat(total).isEqualByComparingTo("216.00");
    }
}
