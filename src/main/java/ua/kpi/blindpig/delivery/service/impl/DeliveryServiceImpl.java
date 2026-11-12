package ua.kpi.blindpig.delivery.service.impl;

import org.springframework.stereotype.Service;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;
import ua.kpi.blindpig.delivery.dto.CourierDto;
import ua.kpi.blindpig.delivery.dto.DeliveryDto;
import ua.kpi.blindpig.delivery.dto.DeliveryStatus;
import ua.kpi.blindpig.delivery.model.Delivery;
import ua.kpi.blindpig.delivery.repository.DeliveryRepository;
import ua.kpi.blindpig.delivery.service.CourierService;
import ua.kpi.blindpig.delivery.service.DeliveryService;
import ua.kpi.blindpig.delivery.service.NotificationService;
import ua.kpi.blindpig.order.dto.FoodOrderDto;
import ua.kpi.blindpig.order.dto.OrderStatus;
import ua.kpi.blindpig.order.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final CourierService courierService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
                               CourierService courierService,
                               OrderService orderService,
                               NotificationService notificationService) {
        this.deliveryRepository = deliveryRepository;
        this.courierService = courierService;
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @Override
    public DeliveryDto createForOrder(Long orderId) {
        if (orderId == null) {
            throw new InvalidRequestException("Order id cannot be null");
        }
        if (deliveryRepository.existsByOrderId(orderId)) {
            throw new BusinessConflictException("Delivery already exists for order #" + orderId);
        }

        FoodOrderDto order = orderService.getById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        if (order.status() != OrderStatus.CONFIRMED) {
            throw new BusinessConflictException("Cannot create delivery for order in status " + order.status()
                    + ". Order must be in CONFIRMED status.");
        }

        Delivery delivery = new Delivery(
                orderId,
                null,
                DeliveryStatus.CREATED,
                LocalDateTime.now(),
                null
        );

        Delivery saved = deliveryRepository.save(delivery);
        return toDto(saved);
    }

    @Override
    public DeliveryDto getById(Long deliveryId) {
        if (deliveryId == null) {
            throw new InvalidRequestException("Delivery id cannot be null");
        }
        return deliveryRepository.findById(deliveryId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));
    }

    @Override
    public DeliveryDto getByOrder(Long orderId) {
        if (orderId == null) {
            throw new InvalidRequestException("Order id cannot be null");
        }
        return deliveryRepository.findByOrderId(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order id: " + orderId));
    }

    @Override
    public List<DeliveryDto> findByCourier(Long courierId) {
        if (courierId == null) {
            throw new InvalidRequestException("Courier id cannot be null");
        }
        return deliveryRepository.findByCourierId(courierId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public DeliveryDto assignCourier(Long deliveryId, Long courierId) {
        if (deliveryId == null) {
            throw new InvalidRequestException("Delivery id cannot be null");
        }
        if (courierId == null) {
            throw new InvalidRequestException("Courier id cannot be null");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        if (delivery.getStatus() != DeliveryStatus.CREATED) {
            throw new BusinessConflictException("Cannot assign courier to delivery in status " + delivery.getStatus()
                    + ". Courier can only be assigned in CREATED status.");
        }

        CourierDto courier = courierService.getById(courierId);
        if (!courier.available()) {
            throw new BusinessConflictException("Courier #" + courierId + " is not currently available");
        }

        courierService.setAvailability(courierId, false);

        delivery.setCourierId(courierId);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        Delivery saved = deliveryRepository.save(delivery);

        return toDto(saved);
    }

    @Override
    public DeliveryDto changeStatus(Long deliveryId, DeliveryStatus nextStatus) {
        if (deliveryId == null) {
            throw new InvalidRequestException("Delivery id cannot be null");
        }
        if (nextStatus == null) {
            throw new InvalidRequestException("Next status cannot be null");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        DeliveryStatus current = delivery.getStatus();

        if (current == DeliveryStatus.DELIVERED) {
            throw new BusinessConflictException("Delivery #" + deliveryId + " has already been delivered (terminal status).");
        }

        if (current == DeliveryStatus.CREATED) {
            throw new BusinessConflictException("Cannot transition from CREATED directly via changeStatus. Use assignCourier first.");
        }

        if (current == DeliveryStatus.ASSIGNED) {
            if (nextStatus != DeliveryStatus.IN_TRANSIT) {
                throw new BusinessConflictException("Invalid transition from ASSIGNED to " + nextStatus + ". Expected IN_TRANSIT.");
            }
            delivery.setStatus(DeliveryStatus.IN_TRANSIT);
        } else if (current == DeliveryStatus.IN_TRANSIT) {
            if (nextStatus != DeliveryStatus.DELIVERED) {
                throw new BusinessConflictException("Invalid transition from IN_TRANSIT to " + nextStatus + ". Expected DELIVERED.");
            }
            delivery.setStatus(DeliveryStatus.DELIVERED);
            delivery.setDeliveredAt(LocalDateTime.now());

            if (delivery.getCourierId() != null) {
                courierService.setAvailability(delivery.getCourierId(), true);
            }

            FoodOrderDto completedOrder = orderService.completeDeliveredOrder(delivery.getOrderId());
            Long customerId = (completedOrder != null) ? completedOrder.customerId() : null;

            if (customerId != null) {
                notificationService.create(
                        customerId,
                        delivery.getOrderId(),
                        "Ваше замовлення #" + delivery.getOrderId() + " успішно доставлено! Смачного!"
                );
            }
        }

        Delivery saved = deliveryRepository.save(delivery);
        return toDto(saved);
    }

    private DeliveryDto toDto(Delivery delivery) {
        return new DeliveryDto(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getCourierId(),
                delivery.getStatus(),
                delivery.getCreatedAt(),
                delivery.getDeliveredAt()
        );
    }
}
