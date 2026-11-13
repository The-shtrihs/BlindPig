package ua.kpi.blindpig.billing.demo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Lab 2: prototype scope — every lookup gets a new instance with its own token. */
@Component
@Scope("prototype")
public class PaymentReceiptNumberGenerator {

    private final String instanceToken = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    public String receiptNumberFor(Long paymentId) {
        return "BP-RCPT-" + paymentId + "-" + instanceToken;
    }

    public String getInstanceToken() {
        return instanceToken;
    }
}
