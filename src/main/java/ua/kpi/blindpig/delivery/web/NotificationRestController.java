package ua.kpi.blindpig.delivery.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.kpi.blindpig.delivery.dto.NotificationDto;
import ua.kpi.blindpig.delivery.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    private final NotificationService notificationService;

    public NotificationRestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/customer/{customerId}")
    public List<NotificationDto> listForCustomer(@PathVariable("customerId") Long customerId) {
        return notificationService.listForCustomer(customerId);
    }

    @GetMapping("/{id}")
    public NotificationDto getById(@PathVariable("id") Long id) {
        return notificationService.getById(id);
    }

    @PatchMapping("/{id}/read")
    public NotificationDto markRead(@PathVariable("id") Long id) {
        return notificationService.markRead(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        notificationService.delete(id);
    }
}
