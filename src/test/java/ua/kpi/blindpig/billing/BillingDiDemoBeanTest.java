package ua.kpi.blindpig.billing;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ua.kpi.blindpig.billing.config.BillingConfig;
import ua.kpi.blindpig.billing.demo.BillingDiDemoBean;
import ua.kpi.blindpig.billing.demo.PaymentReceiptNumberGenerator;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.billing.service.PaymentService;
import ua.kpi.blindpig.billing.service.PromoCodeService;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Verifies the lab-2 DI demo wiring in a small Spring context (no OrderService needed). */
class BillingDiDemoBeanTest {

    @Test
    void allInjectionStylesAndScopesShouldWork() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(DiscountService.class, () -> mock(DiscountService.class));
            ctx.registerBean(PromoCodeService.class, () -> mock(PromoCodeService.class));
            ctx.registerBean(PaymentService.class, () -> mock(PaymentService.class));
            ctx.register(BillingConfig.class, BillingDiDemoBean.class, PaymentReceiptNumberGenerator.class);
            ctx.refresh();

            BillingDiDemoBean demo = ctx.getBean(BillingDiDemoBean.class);
            assertThat(demo.isInitialized()).isTrue();
            assertThat(demo.getConstructorInjectedDiscountService()).isSameAs(ctx.getBean(DiscountService.class));
            assertThat(demo.getSetterInjectedPaymentService()).isSameAs(ctx.getBean(PaymentService.class));
            assertThat(demo.getFieldInjectedPromoCodeService()).isSameAs(ctx.getBean(PromoCodeService.class));

            // singleton: same instance every time
            assertThat(ctx.getBean(BillingDiDemoBean.class)).isSameAs(demo);
            assertThat(ctx.getBean(Clock.class)).isSameAs(ctx.getBean("billingClock"));

            // prototype: new instance per lookup
            PaymentReceiptNumberGenerator first = demo.newReceiptGenerator();
            PaymentReceiptNumberGenerator second = demo.newReceiptGenerator();
            assertThat(first).isNotSameAs(second);
            assertThat(first.receiptNumberFor(5L)).startsWith("BP-RCPT-5-");
        }
    }
}
