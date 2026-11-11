package ua.kpi.blindpig.delivery.service;

import ua.kpi.blindpig.delivery.dto.*;
import java.util.List;

public interface CourierService {
    CourierDto create(SaveCourierRequest request);
    CourierDto getById(Long courierId);
    List<CourierDto> list();
    List<CourierDto> findAvailable();
    CourierDto update(Long courierId, SaveCourierRequest request);
    /** Used by DeliveryService when assigning/completing delivery. */
    CourierDto setAvailability(Long courierId, boolean available);
    void delete(Long courierId);
}
