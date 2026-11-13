package ua.kpi.blindpig.billing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Lab 2: explicit {@code @Bean} definition. Billing services read "now" from this clock so tests can fix time. */
@Configuration
public class BillingConfig {

    @Bean
    public Clock billingClock() {
        return Clock.systemDefaultZone();
    }
}
