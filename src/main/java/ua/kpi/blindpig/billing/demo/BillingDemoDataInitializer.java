package ua.kpi.blindpig.billing.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.billing.dto.SaveDiscountRequest;
import ua.kpi.blindpig.billing.dto.SavePromoCodeRequest;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.billing.service.PromoCodeService;

import java.math.BigDecimal;

/** Seeds demo promotions for the in-memory stage (labs 2–4). Remove once data lives in PostgreSQL. */
@Component
public class BillingDemoDataInitializer implements ApplicationRunner {

    private final DiscountService discountService;
    private final PromoCodeService promoCodeService;

    public BillingDemoDataInitializer(DiscountService discountService, PromoCodeService promoCodeService) {
        this.discountService = discountService;
        this.promoCodeService = promoCodeService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!discountService.list().isEmpty() || !promoCodeService.list().isEmpty()) {
            return;
        }
        discountService.create(new SaveDiscountRequest("Піца тижня −20%", DiscountType.PERCENTAGE,
                new BigDecimal("20"), 1L, null, null, null, true));
        discountService.create(new SaveDiscountRequest("Категорія −15%", DiscountType.PERCENTAGE,
                new BigDecimal("15"), null, 1L, null, null, true));
        promoCodeService.create(new SavePromoCodeRequest("WELCOME10", DiscountType.PERCENTAGE,
                new BigDecimal("10"), null, null, null, true));
        promoCodeService.create(new SavePromoCodeRequest("MINUS50", DiscountType.FIXED,
                new BigDecimal("50"), new BigDecimal("300"), null, null, true));
    }
}
