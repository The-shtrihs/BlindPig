package ua.kpi.blindpig.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;
import ua.kpi.blindpig.delivery.dto.CourierDto;
import ua.kpi.blindpig.delivery.dto.SaveCourierRequest;
import ua.kpi.blindpig.delivery.repository.inmemory.InMemoryCourierRepository;
import ua.kpi.blindpig.delivery.repository.inmemory.InMemoryDeliveryRepository;
import ua.kpi.blindpig.delivery.service.CourierService;
import ua.kpi.blindpig.delivery.service.impl.CourierServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourierServiceImplTest {

    private InMemoryCourierRepository courierRepository;
    private InMemoryDeliveryRepository deliveryRepository;
    private CourierService courierService;

    @BeforeEach
    void setUp() {
        courierRepository = new InMemoryCourierRepository();
        deliveryRepository = new InMemoryDeliveryRepository();
        courierService = new CourierServiceImpl(courierRepository, deliveryRepository);
    }

    @Test
    void shouldCreateAndRetrieveCourier() {
        SaveCourierRequest request = new SaveCourierRequest("Тарас", "+380501112233", true);
        CourierDto created = courierService.create(request);

        assertNotNull(created.id());
        assertEquals("Тарас", created.name());
        assertEquals("+380501112233", created.phone());
        assertTrue(created.available());

        CourierDto found = courierService.getById(created.id());
        assertEquals(created.id(), found.id());
        assertEquals("Тарас", found.name());
    }

    @Test
    void shouldThrowInvalidRequestOnBlankNameOrPhone() {
        assertThrows(InvalidRequestException.class, () ->
                courierService.create(new SaveCourierRequest("", "+380501112233", true)));
        assertThrows(InvalidRequestException.class, () ->
                courierService.create(new SaveCourierRequest("Тарас", "   ", true)));
    }

    @Test
    void shouldFilterAvailableCouriers() {
        courierService.create(new SaveCourierRequest("Вільний 1", "+380501111111", true));
        CourierDto busy = courierService.create(new SaveCourierRequest("Зайнятий", "+380502222222", true));
        courierService.setAvailability(busy.id(), false);

        List<CourierDto> available = courierService.findAvailable();
        assertEquals(1, available.size());
        assertEquals("Вільний 1", available.getFirst().name());
    }

    @Test
    void shouldUpdateCourier() {
        CourierDto created = courierService.create(new SaveCourierRequest("Олег", "+380503333333", true));

        CourierDto updated = courierService.update(created.id(), new SaveCourierRequest("Олег Іванович", "+380504444444", false));
        assertEquals("Олег Іванович", updated.name());
        assertEquals("+380504444444", updated.phone());
        assertFalse(updated.available());
    }

    @Test
    void shouldDeleteCourierWithoutDeliveries() {
        CourierDto created = courierService.create(new SaveCourierRequest("Степан", "+380505555555", true));
        courierService.delete(created.id());

        assertThrows(ResourceNotFoundException.class, () -> courierService.getById(created.id()));
    }

    @Test
    void shouldThrowConflictWhenDeletingCourierWithDeliveries() {
        CourierDto created = courierService.create(new SaveCourierRequest("Степан", "+380505555555", true));

        ua.kpi.blindpig.delivery.model.Delivery delivery = new ua.kpi.blindpig.delivery.model.Delivery(
                10L, created.id(), ua.kpi.blindpig.delivery.dto.DeliveryStatus.DELIVERED,
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );
        deliveryRepository.save(delivery);

        assertThrows(BusinessConflictException.class, () -> courierService.delete(created.id()));
    }
}
