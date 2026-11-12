package ua.kpi.blindpig.delivery.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.kpi.blindpig.common.ResourceNotFoundException;
import ua.kpi.blindpig.delivery.dto.CourierDto;
import ua.kpi.blindpig.delivery.dto.DeliveryDto;
import ua.kpi.blindpig.delivery.dto.DeliveryStatus;
import ua.kpi.blindpig.delivery.dto.SaveCourierRequest;
import ua.kpi.blindpig.delivery.service.CourierService;
import ua.kpi.blindpig.delivery.service.DeliveryService;
import ua.kpi.blindpig.delivery.service.NotificationService;

@Controller
@RequestMapping("/deliveries")
public class DeliveryPageController {

    private final CourierService courierService;
    private final DeliveryService deliveryService;
    private final NotificationService notificationService;

    public DeliveryPageController(CourierService courierService,
                                  DeliveryService deliveryService,
                                  NotificationService notificationService) {
        this.courierService = courierService;
        this.deliveryService = deliveryService;
        this.notificationService = notificationService;
    }

    @GetMapping({"", "/"})
    public String deliveries(@RequestParam(name = "orderId", required = false) Long orderId,
                             @RequestParam(name = "courierId", required = false) Long courierId,
                             Model model) {
        model.addAttribute("availableCouriers", courierService.findAvailable());
        model.addAttribute("allCouriers", courierService.list());

        if (orderId != null) {
            model.addAttribute("searchedOrderId", orderId);
            try {
                DeliveryDto delivery = deliveryService.getByOrder(orderId);
                model.addAttribute("selectedDelivery", delivery);
            } catch (ResourceNotFoundException e) {
                model.addAttribute("searchError", "Доставку для замовлення #" + orderId + " не знайдено.");
            }
        }

        if (courierId != null) {
            model.addAttribute("selectedCourierId", courierId);
            model.addAttribute("courierDeliveries", deliveryService.findByCourier(courierId));
        }

        return "delivery/deliveries";
    }

    @PostMapping
    public String createDelivery(@RequestParam("orderId") Long orderId) {
        deliveryService.createForOrder(orderId);
        return "redirect:/deliveries?orderId=" + orderId;
    }

    @PostMapping("/{id}/assign")
    public String assignCourier(@PathVariable("id") Long id,
                                @RequestParam("courierId") Long courierId,
                                @RequestParam(name = "orderId", required = false) Long orderId) {
        deliveryService.assignCourier(id, courierId);
        return "redirect:/deliveries" + (orderId != null ? "?orderId=" + orderId : "");
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable("id") Long id,
                               @RequestParam("status") DeliveryStatus status,
                               @RequestParam(name = "orderId", required = false) Long orderId) {
        deliveryService.changeStatus(id, status);
        return "redirect:/deliveries" + (orderId != null ? "?orderId=" + orderId : "");
    }

    @GetMapping("/couriers")
    public String couriers(Model model) {
        model.addAttribute("couriers", courierService.list());
        model.addAttribute("courierForm", new SaveCourierRequest("", "", true));
        return "delivery/couriers";
    }

    @PostMapping("/couriers")
    public String createCourier(@ModelAttribute("courierForm") SaveCourierRequest request) {
        courierService.create(request);
        return "redirect:/deliveries/couriers";
    }

    @PostMapping("/couriers/{id}/toggle-availability")
    public String toggleCourierAvailability(@PathVariable("id") Long id) {
        CourierDto courier = courierService.getById(id);
        courierService.setAvailability(id, !courier.available());
        return "redirect:/deliveries/couriers";
    }

    @GetMapping("/notifications")
    public String notifications(@RequestParam(name = "customerId", required = false, defaultValue = "1") Long customerId,
                                Model model) {
        model.addAttribute("customerId", customerId);
        model.addAttribute("notifications", notificationService.listForCustomer(customerId));
        return "delivery/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationRead(@PathVariable("id") Long id, @RequestParam("customerId") Long customerId) {
        notificationService.markRead(id);
        return "redirect:/deliveries/notifications?customerId=" + customerId;
    }
}
