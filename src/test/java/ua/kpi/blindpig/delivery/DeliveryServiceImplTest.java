package ua.kpi.blindpig.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.delivery.dto.CourierDto;
import ua.kpi.blindpig.delivery.dto.DeliveryDto;
import ua.kpi.blindpig.delivery.dto.DeliveryStatus;
import ua.kpi.blindpig.delivery.dto.SaveCourierRequest;
import ua.kpi.blindpig.delivery.repository.inmemory.InMemoryCourierRepository;
import ua.kpi.blindpig.delivery.repository.inmemory.InMemoryDeliveryRepository;
import ua.kpi.blindpig.delivery.repository.inmemory.InMemoryNotificationRepository;
import ua.kpi.blindpig.delivery.service.CourierService;
import ua.kpi.blindpig.delivery.service.DeliveryService;
import ua.kpi.blindpig.delivery.service.NotificationService;
import ua.kpi.blindpig.delivery.service.impl.CourierServiceImpl;
import ua.kpi.blindpig.delivery.service.impl.DeliveryServiceImpl;
import ua.kpi.blindpig.delivery.service.impl.NotificationServiceImpl;
import ua.kpi.blindpig.order.dto.FoodOrderDto;
import ua.kpi.blindpig.order.dto.OrderStatus;
import ua.kpi.blindpig.order.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeliveryServiceImplTest {

    private InMemoryDeliveryRepository deliveryRepository;
    private CourierService courierService;
    private NotificationService notificationService;
    private OrderService orderService;
    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryRepository = new InMemoryDeliveryRepository();
        InMemoryCourierRepository courierRepository = new InMemoryCourierRepository();
        InMemoryNotificationRepository notificationRepository = new InMemoryNotificationRepository();

        courierService = new CourierServiceImpl(courierRepository, deliveryRepository);
        notificationService = new NotificationServiceImpl(notificationRepository);
        orderService = Mockito.mock(OrderService.class);

        deliveryService = new DeliveryServiceImpl(deliveryRepository, courierService, orderService, notificationService);
    }

    private FoodOrderDto createMockOrder(Long orderId, OrderStatus status) {
        return new FoodOrderDto(
                orderId,
                42L,
                "вул. Політехнічна, 14",
                status,
                List.of(),
                new BigDecimal("350.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("350.00"),
                LocalDateTime.now()
        );
    }

    @Test
    void shouldCreateDeliveryForConfirmedOrder() {
        when(orderService.getById(100L)).thenReturn(createMockOrder(100L, OrderStatus.CONFIRMED));

        DeliveryDto delivery = deliveryService.createForOrder(100L);

        assertNotNull(delivery.id());
        assertEquals(100L, delivery.orderId());
        assertNull(delivery.courierId());
        assertEquals(DeliveryStatus.CREATED, delivery.status());
        assertNotNull(delivery.createdAt());
        assertNull(delivery.deliveredAt());
    }

    @Test
    void shouldThrowConflictWhenOrderIsNotConfirmed() {
        when(orderService.getById(101L)).thenReturn(createMockOrder(101L, OrderStatus.CREATED));

        assertThrows(BusinessConflictException.class, () -> deliveryService.createForOrder(101L));
    }

    @Test
    void shouldThrowConflictWhenDeliveryAlreadyExistsForOrder() {
        when(orderService.getById(102L)).thenReturn(createMockOrder(102L, OrderStatus.CONFIRMED));

        deliveryService.createForOrder(102L);

        assertThrows(BusinessConflictException.class, () -> deliveryService.createForOrder(102L));
    }

    @Test
    void shouldAssignAvailableCourierSuccessfully() {
        when(orderService.getById(200L)).thenReturn(createMockOrder(200L, OrderStatus.CONFIRMED));
        DeliveryDto delivery = deliveryService.createForOrder(200L);

        CourierDto courier = courierService.create(new SaveCourierRequest("Ігор", "+380509998877", true));

        DeliveryDto assigned = deliveryService.assignCourier(delivery.id(), courier.id());

        assertEquals(DeliveryStatus.ASSIGNED, assigned.status());
        assertEquals(courier.id(), assigned.courierId());

        CourierDto updatedCourier = courierService.getById(courier.id());
        assertFalse(updatedCourier.available());
    }

    @Test
    void shouldThrowConflictWhenAssigningBusyCourier() {
        when(orderService.getById(201L)).thenReturn(createMockOrder(201L, OrderStatus.CONFIRMED));
        DeliveryDto delivery = deliveryService.createForOrder(201L);

        CourierDto courier = courierService.create(new SaveCourierRequest("Василь", "+380507776655", false));

        assertThrows(BusinessConflictException.class, () -> deliveryService.assignCourier(delivery.id(), courier.id()));
    }

    @Test
    void shouldTransitionStatusesUntilDelivered() {
        when(orderService.getById(300L)).thenReturn(createMockOrder(300L, OrderStatus.CONFIRMED));
        when(orderService.completeDeliveredOrder(300L)).thenReturn(createMockOrder(300L, OrderStatus.COMPLETED));

        DeliveryDto delivery = deliveryService.createForOrder(300L);
        CourierDto courier = courierService.create(new SaveCourierRequest("Андрій", "+380501234567", true));

        delivery = deliveryService.assignCourier(delivery.id(), courier.id());
        assertEquals(DeliveryStatus.ASSIGNED, delivery.status());

        delivery = deliveryService.changeStatus(delivery.id(), DeliveryStatus.IN_TRANSIT);
        assertEquals(DeliveryStatus.IN_TRANSIT, delivery.status());

        delivery = deliveryService.changeStatus(delivery.id(), DeliveryStatus.DELIVERED);
        assertEquals(DeliveryStatus.DELIVERED, delivery.status());
        assertNotNull(delivery.deliveredAt());

        assertTrue(courierService.getById(courier.id()).available());

        verify(orderService, times(1)).completeDeliveredOrder(300L);

        assertEquals(1, notificationService.listForCustomer(42L).size());
    }

    @Test
    void shouldRejectInvalidTransitions() {
        when(orderService.getById(400L)).thenReturn(createMockOrder(400L, OrderStatus.CONFIRMED));
        final DeliveryDto createdDelivery = deliveryService.createForOrder(400L);

        assertThrows(BusinessConflictException.class, () ->
                deliveryService.changeStatus(createdDelivery.id(), DeliveryStatus.IN_TRANSIT));

        CourierDto courier = courierService.create(new SaveCourierRequest("Петро", "+380509876543", true));
        final DeliveryDto assignedDelivery = deliveryService.assignCourier(createdDelivery.id(), courier.id());

        assertThrows(BusinessConflictException.class, () ->
                deliveryService.changeStatus(assignedDelivery.id(), DeliveryStatus.DELIVERED));
    }
}
