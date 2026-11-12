package ua.kpi.blindpig.delivery.web;

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

@RestController
@RequestMapping("/api/couriers")
public class CourierRestController {

    private final CourierService courierService;

    public CourierRestController(CourierService courierService) {
        this.courierService = courierService;
    }

    @GetMapping
    public List<CourierDto> list() {
        return courierService.list();
    }

    @GetMapping("/available")
    public List<CourierDto> findAvailable() {
        return courierService.findAvailable();
    }

    @GetMapping("/{id}")
    public CourierDto getById(@PathVariable("id") Long id) {
        return courierService.getById(id);
    }

    @PostMapping
    public ResponseEntity<CourierDto> create(@Valid @RequestBody SaveCourierRequest request) {
        CourierDto created = courierService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public CourierDto update(@PathVariable("id") Long id, @Valid @RequestBody SaveCourierRequest request) {
        return courierService.update(id, request);
    }

    @PatchMapping("/{id}/availability")
    public CourierDto setAvailability(@PathVariable("id") Long id, @RequestParam("available") boolean available) {
        return courierService.setAvailability(id, available);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        courierService.delete(id);
    }
}
