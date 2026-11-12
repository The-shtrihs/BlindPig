package ua.kpi.blindpig.delivery.service;

import ua.kpi.blindpig.delivery.dto.DeliveryDto;
import ua.kpi.blindpig.delivery.dto.DeliveryStatus;

import java.util.List;

public interface DeliveryService {
    DeliveryDto createForOrder(Long orderId);
    DeliveryDto getById(Long deliveryId);
    DeliveryDto getByOrder(Long orderId);
    List<DeliveryDto> findByCourier(Long courierId);
    DeliveryDto assignCourier(Long deliveryId, Long courierId);
    DeliveryDto changeStatus(Long deliveryId, DeliveryStatus nextStatus);
}
