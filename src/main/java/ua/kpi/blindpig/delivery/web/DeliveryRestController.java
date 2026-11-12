package ua.kpi.blindpig.delivery.web;

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

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryRestController {

    private final DeliveryService deliveryService;

    public DeliveryRestController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public ResponseEntity<DeliveryDto> create(@RequestParam("orderId") Long orderId) {
        DeliveryDto created = deliveryService.createForOrder(orderId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public DeliveryDto getById(@PathVariable("id") Long id) {
        return deliveryService.getById(id);
    }

    @GetMapping("/order/{orderId}")
    public DeliveryDto getByOrder(@PathVariable("orderId") Long orderId) {
        return deliveryService.getByOrder(orderId);
    }

    @GetMapping("/courier/{courierId}")
    public List<DeliveryDto> findByCourier(@PathVariable("courierId") Long courierId) {
        return deliveryService.findByCourier(courierId);
    }

    @PostMapping("/{id}/assign")
    public DeliveryDto assignCourier(@PathVariable("id") Long id, @RequestParam("courierId") Long courierId) {
        return deliveryService.assignCourier(id, courierId);
    }

    @PatchMapping("/{id}/status")
    public DeliveryDto changeStatus(@PathVariable("id") Long id, @RequestParam("status") DeliveryStatus status) {
        return deliveryService.changeStatus(id, status);
    }
}
