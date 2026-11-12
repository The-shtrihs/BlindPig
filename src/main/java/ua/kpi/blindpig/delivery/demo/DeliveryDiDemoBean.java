package ua.kpi.blindpig.delivery.demo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.kpi.blindpig.delivery.service.CourierService;
import ua.kpi.blindpig.delivery.service.DeliveryService;
import ua.kpi.blindpig.delivery.service.NotificationService;

@Component
public class DeliveryDiDemoBean {

    @Autowired
    private NotificationService fieldInjectedNotificationService;

    private final CourierService constructorInjectedCourierService;

    private DeliveryService setterInjectedDeliveryService;

    private final ObjectProvider<DeliveryTrackingNumberGenerator> trackingNumberGeneratorProvider;

    private boolean initialized = false;

    public DeliveryDiDemoBean(CourierService constructorInjectedCourierService,
                              ObjectProvider<DeliveryTrackingNumberGenerator> trackingNumberGeneratorProvider) {
        this.constructorInjectedCourierService = constructorInjectedCourierService;
        this.trackingNumberGeneratorProvider = trackingNumberGeneratorProvider;
    }

    @Autowired
    public void setSetterInjectedDeliveryService(DeliveryService setterInjectedDeliveryService) {
        this.setterInjectedDeliveryService = setterInjectedDeliveryService;
    }

    @PostConstruct
    public void onInit() {
        this.initialized = true;
    }

    @PreDestroy
    public void onDestroy() {
        this.initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public CourierService getConstructorInjectedCourierService() {
        return constructorInjectedCourierService;
    }

    public DeliveryService getSetterInjectedDeliveryService() {
        return setterInjectedDeliveryService;
    }

    public NotificationService getFieldInjectedNotificationService() {
        return fieldInjectedNotificationService;
    }

    public DeliveryTrackingNumberGenerator getNewTrackingGenerator() {
        return trackingNumberGeneratorProvider.getObject();
    }
}
