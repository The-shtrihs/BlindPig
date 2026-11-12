package ua.kpi.blindpig.delivery.service.impl;

import org.springframework.stereotype.Service;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;
import ua.kpi.blindpig.delivery.dto.CourierDto;
import ua.kpi.blindpig.delivery.dto.SaveCourierRequest;
import ua.kpi.blindpig.delivery.model.Courier;
import ua.kpi.blindpig.delivery.repository.CourierRepository;
import ua.kpi.blindpig.delivery.repository.DeliveryRepository;
import ua.kpi.blindpig.delivery.service.CourierService;

import java.util.List;

@Service
public class CourierServiceImpl implements CourierService {

    private final CourierRepository courierRepository;
    private final DeliveryRepository deliveryRepository;

    public CourierServiceImpl(CourierRepository courierRepository, DeliveryRepository deliveryRepository) {
        this.courierRepository = courierRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    public CourierDto create(SaveCourierRequest request) {
        validateRequest(request);
        Courier courier = new Courier(request.name().trim(), request.phone().trim(), request.available());
        Courier saved = courierRepository.save(courier);
        return toDto(saved);
    }

    @Override
    public CourierDto getById(Long courierId) {
        if (courierId == null) {
            throw new InvalidRequestException("Courier id cannot be null");
        }
        return courierRepository.findById(courierId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found with id: " + courierId));
    }

    @Override
    public List<CourierDto> list() {
        return courierRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<CourierDto> findAvailable() {
        return courierRepository.findByAvailable(true).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CourierDto update(Long courierId, SaveCourierRequest request) {
        validateRequest(request);
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found with id: " + courierId));

        courier.setName(request.name().trim());
        courier.setPhone(request.phone().trim());
        courier.setAvailable(request.available());

        Courier saved = courierRepository.save(courier);
        return toDto(saved);
    }

    @Override
    public CourierDto setAvailability(Long courierId, boolean available) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found with id: " + courierId));

        courier.setAvailable(available);
        Courier saved = courierRepository.save(courier);
        return toDto(saved);
    }

    @Override
    public void delete(Long courierId) {
        if (!courierRepository.existsById(courierId)) {
            throw new ResourceNotFoundException("Courier not found with id: " + courierId);
        }
        if (deliveryRepository.existsByCourierId(courierId)) {
            throw new BusinessConflictException("Cannot delete courier #" + courierId + " with existing deliveries");
        }
        courierRepository.deleteById(courierId);
    }

    private void validateRequest(SaveCourierRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Courier request cannot be null");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidRequestException("Courier name cannot be blank");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            throw new InvalidRequestException("Courier phone cannot be blank");
        }
    }

    private CourierDto toDto(Courier courier) {
        return new CourierDto(courier.getId(), courier.getName(), courier.getPhone(), courier.isAvailable());
    }
}
