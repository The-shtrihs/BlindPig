package ua.kpi.blindpig.delivery.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.kpi.blindpig.delivery.dto.CourierDto;
import ua.kpi.blindpig.delivery.dto.SaveCourierRequest;
import ua.kpi.blindpig.delivery.service.CourierService;

import java.net.URI;
import java.util.List;

@Tag(name = "Couriers", description = "Керування кур'єрами та їх зайнятістю")
@RestController
@RequestMapping("/api/couriers")
public class CourierRestController {

    private final CourierService courierService;

    public CourierRestController(CourierService courierService) {
        this.courierService = courierService;
    }

    @Operation(summary = "Отримати список усіх кур'єрів")
    @ApiResponse(responseCode = "200", description = "Список кур'єрів успішно отримано")
    @GetMapping
    public List<CourierDto> list() {
        return courierService.list();
    }

    @Operation(summary = "Отримати список доступних кур'єрів")
    @ApiResponse(responseCode = "200", description = "Список вільних кур'єрів успішно отримано")
    @GetMapping("/available")
    public List<CourierDto> findAvailable() {
        return courierService.findAvailable();
    }

    @Operation(summary = "Отримати кур'єра за його ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Кур'єра знайдено"),
            @ApiResponse(responseCode = "404", description = "Кур'єра не знайдено")
    })
    @GetMapping("/{id}")
    public CourierDto getById(@PathVariable("id") Long id) {
        return courierService.getById(id);
    }

    @Operation(summary = "Створити нового кур'єра")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Кур'єра успішно створено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані")
    })
    @PostMapping
    public ResponseEntity<CourierDto> create(@Valid @RequestBody SaveCourierRequest request) {
        CourierDto created = courierService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Повне оновлення даних кур'єра")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Кур'єра успішно оновлено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані"),
            @ApiResponse(responseCode = "404", description = "Кур'єра не знайдено")
    })
    @PutMapping("/{id}")
    public CourierDto update(@PathVariable("id") Long id, @Valid @RequestBody SaveCourierRequest request) {
        return courierService.update(id, request);
    }

    @Operation(summary = "Змінити статус доступності кур'єра")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус доступності оновлено"),
            @ApiResponse(responseCode = "404", description = "Кур'єра не знайдено")
    })
    @PatchMapping("/{id}/availability")
    public CourierDto setAvailability(@PathVariable("id") Long id, @RequestParam("available") boolean available) {
        return courierService.setAvailability(id, available);
    }

    @Operation(summary = "Видалити кур'єра")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Кур'єра успішно видалено"),
            @ApiResponse(responseCode = "404", description = "Кур'єра не знайдено"),
            @ApiResponse(responseCode = "409", description = "Конфлікт: кур'єр має історію доставок")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        courierService.delete(id);
    }
}
