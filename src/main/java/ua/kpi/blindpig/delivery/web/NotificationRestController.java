package ua.kpi.blindpig.delivery.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Notifications", description = "Центр in-app сповіщень користувачів")
@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    private final NotificationService notificationService;

    public NotificationRestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Отримати всі сповіщення конкретного клієнта")
    @ApiResponse(responseCode = "200", description = "Список сповіщень успішно отримано")
    @GetMapping("/customer/{customerId}")
    public List<NotificationDto> listForCustomer(@PathVariable("customerId") Long customerId) {
        return notificationService.listForCustomer(customerId);
    }

    @Operation(summary = "Отримати сповіщення за його ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сповіщення знайдено"),
            @ApiResponse(responseCode = "404", description = "Сповіщення не знайдено")
    })
    @GetMapping("/{id}")
    public NotificationDto getById(@PathVariable("id") Long id) {
        return notificationService.getById(id);
    }

    @Operation(summary = "Позначити сповіщення як прочитане")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сповіщення успішно позначено як прочитане"),
            @ApiResponse(responseCode = "404", description = "Сповіщення не знайдено")
    })
    @PatchMapping("/{id}/read")
    public NotificationDto markRead(@PathVariable("id") Long id) {
        return notificationService.markRead(id);
    }

    @Operation(summary = "Видалити сповіщення")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Сповіщення успішно видалено"),
            @ApiResponse(responseCode = "404", description = "Сповіщення не знайдено")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        notificationService.delete(id);
    }
}
