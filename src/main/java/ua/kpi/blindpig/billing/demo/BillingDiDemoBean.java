package ua.kpi.blindpig.billing.demo;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.billing.service.PaymentService;
import ua.kpi.blindpig.billing.service.PromoCodeService;

/**
 * Lab 2 demonstration of the three injection styles plus singleton vs prototype scope.
 * Production billing code uses constructor injection only.
 */
@Component
public class BillingDiDemoBean {

    /** Field injection. */
    @Autowired
    private PromoCodeService fieldInjectedPromoCodeService;

    /** Constructor injection. */
    private final DiscountService constructorInjectedDiscountService;

    /** Prototype beans are obtained lazily so each call creates a fresh instance. */
    private final ObjectProvider<PaymentReceiptNumberGenerator> receiptGeneratorProvider;

    /** Setter injection. */
    private PaymentService setterInjectedPaymentService;

    private boolean initialized;

    public BillingDiDemoBean(DiscountService constructorInjectedDiscountService,
                             ObjectProvider<PaymentReceiptNumberGenerator> receiptGeneratorProvider) {
        this.constructorInjectedDiscountService = constructorInjectedDiscountService;
        this.receiptGeneratorProvider = receiptGeneratorProvider;
    }

    @Autowired
    public void setSetterInjectedPaymentService(PaymentService setterInjectedPaymentService) {
        this.setterInjectedPaymentService = setterInjectedPaymentService;
    }

    @PostConstruct
    void init() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public PromoCodeService getFieldInjectedPromoCodeService() {
        return fieldInjectedPromoCodeService;
    }

    public DiscountService getConstructorInjectedDiscountService() {
        return constructorInjectedDiscountService;
    }

    public PaymentService getSetterInjectedPaymentService() {
        return setterInjectedPaymentService;
    }

    public PaymentReceiptNumberGenerator newReceiptGenerator() {
        return receiptGeneratorProvider.getObject();
    }
}
