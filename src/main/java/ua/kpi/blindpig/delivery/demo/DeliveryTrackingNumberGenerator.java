package ua.kpi.blindpig.delivery.demo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope("prototype")
public class DeliveryTrackingNumberGenerator {

    private final String trackingPrefix = "BP-TRK-";
    private final String uniqueToken = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    public String generateTrackingCode(Long deliveryId) {
        return trackingPrefix + deliveryId + "-" + uniqueToken;
    }

    public String getUniqueToken() {
        return uniqueToken;
    }
}
