package ua.kpi.blindpig.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;
import ua.kpi.blindpig.delivery.dto.NotificationDto;
import ua.kpi.blindpig.delivery.repository.inmemory.InMemoryNotificationRepository;
import ua.kpi.blindpig.delivery.service.NotificationService;
import ua.kpi.blindpig.delivery.service.impl.NotificationServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceImplTest {

    private InMemoryNotificationRepository notificationRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = new InMemoryNotificationRepository();
        notificationService = new NotificationServiceImpl(notificationRepository);
    }

    @Test
    void shouldCreateAndListNotificationsForCustomer() {
        notificationService.create(1L, 10L, "Замовлення оформлено");
        notificationService.create(1L, 10L, "Кур'єр виїхав");
        notificationService.create(2L, 11L, "Сповіщення іншому клієнту");

        List<NotificationDto> notifications = notificationService.listForCustomer(1L);
        assertEquals(2, notifications.size());
        assertFalse(notifications.get(0).read());
        assertFalse(notifications.get(1).read());
    }

    @Test
    void shouldMarkNotificationAsReadIdempotently() {
        NotificationDto created = notificationService.create(1L, null, "Системне повідомлення");
        assertFalse(created.read());

        NotificationDto readOnce = notificationService.markRead(created.id());
        assertTrue(readOnce.read());

        NotificationDto readTwice = notificationService.markRead(created.id());
        assertTrue(readTwice.read());
    }

    @Test
    void shouldDeleteNotification() {
        NotificationDto created = notificationService.create(1L, 10L, "Тимчасове");
        notificationService.delete(created.id());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.getById(created.id()));
    }

    @Test
    void shouldThrowInvalidRequestOnBlankMessage() {
        assertThrows(InvalidRequestException.class, () ->
                notificationService.create(1L, 10L, "   "));
    }
}
