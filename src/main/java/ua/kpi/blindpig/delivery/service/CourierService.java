package ua.kpi.blindpig.delivery.service;

import ua.kpi.blindpig.delivery.dto.CourierDto;
import ua.kpi.blindpig.delivery.dto.SaveCourierRequest;

import java.util.List;

public interface CourierService {
    CourierDto create(SaveCourierRequest request);
    CourierDto getById(Long courierId);
    List<CourierDto> list();
    List<CourierDto> findAvailable();
    CourierDto update(Long courierId, SaveCourierRequest request);
    CourierDto setAvailability(Long courierId, boolean available);
    void delete(Long courierId);
}
