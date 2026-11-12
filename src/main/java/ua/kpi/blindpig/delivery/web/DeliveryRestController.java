package ua.kpi.blindpig.delivery.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.kpi.blindpig.delivery.dto.DeliveryDto;
import ua.kpi.blindpig.delivery.dto.DeliveryStatus;
import ua.kpi.blindpig.delivery.service.DeliveryService;

import java.net.URI;
import java.util.List;

@Tag(name = "Deliveries", description = "Керування життєвим циклом доставок замовлень")
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryRestController {

    private final DeliveryService deliveryService;

    public DeliveryRestController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Operation(summary = "Створити доставку для підтвердженого замовлення")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Доставку успішно створено"),
            @ApiResponse(responseCode = "404", description = "Замовлення не знайдено"),
            @ApiResponse(responseCode = "409", description = "Конфлікт: замовлення не підтверджено або доставка вже існує")
    })
    @PostMapping
    public ResponseEntity<DeliveryDto> create(@RequestParam("orderId") Long orderId) {
        DeliveryDto created = deliveryService.createForOrder(orderId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Отримати інформацію про доставку за її ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Доставку знайдено"),
            @ApiResponse(responseCode = "404", description = "Доставку не знайдено")
    })
    @GetMapping("/{id}")
    public DeliveryDto getById(@PathVariable("id") Long id) {
        return deliveryService.getById(id);
    }

    @Operation(summary = "Отримати доставку за номером замовлення (Order ID)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Доставку знайдено"),
            @ApiResponse(responseCode = "404", description = "Доставку не знайдено")
    })
    @GetMapping("/order/{orderId}")
    public DeliveryDto getByOrder(@PathVariable("orderId") Long orderId) {
        return deliveryService.getByOrder(orderId);
    }

    @Operation(summary = "Отримати всі доставки, закріплені за кур'єром")
    @ApiResponse(responseCode = "200", description = "Список доставок кур'єра успішно отримано")
    @GetMapping("/courier/{courierId}")
    public List<DeliveryDto> findByCourier(@PathVariable("courierId") Long courierId) {
        return deliveryService.findByCourier(courierId);
    }

    @Operation(summary = "Призначити вільного кур'єра на доставку")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Кур'єра успішно призначено"),
            @ApiResponse(responseCode = "404", description = "Доставку або кур'єра не знайдено"),
            @ApiResponse(responseCode = "409", description = "Конфлікт: кур'єр зайнятий або статус доставки не CREATED")
    })
    @PostMapping("/{id}/assign")
    public DeliveryDto assignCourier(@PathVariable("id") Long id, @RequestParam("courierId") Long courierId) {
        return deliveryService.assignCourier(id, courierId);
    }

    @Operation(summary = "Змінити статус доставки (IN_TRANSIT, DELIVERED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус доставки успішно змінено"),
            @ApiResponse(responseCode = "404", description = "Доставку не знайдено"),
            @ApiResponse(responseCode = "409", description = "Конфлікт: невалідний перехід статусу")
    })
    @PatchMapping("/{id}/status")
    public DeliveryDto changeStatus(@PathVariable("id") Long id, @RequestParam("status") DeliveryStatus status) {
        return deliveryService.changeStatus(id, status);
    }
}
