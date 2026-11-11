package ua.kpi.blindpig.delivery.service;

import ua.kpi.blindpig.delivery.dto.*;
import java.util.List;

public interface DeliveryService {
    /** Requires a CONFIRMED order; one delivery per order. */
    DeliveryDto createForOrder(Long orderId);
    DeliveryDto getById(Long deliveryId);
    DeliveryDto getByOrder(Long orderId);
    List<DeliveryDto> findByCourier(Long courierId);
    DeliveryDto assignCourier(Long deliveryId, Long courierId);
    /** Validates CREATED -> ASSIGNED -> IN_TRANSIT -> DELIVERED; marks order completed. */
    DeliveryDto changeStatus(Long deliveryId, DeliveryStatus nextStatus);
}
