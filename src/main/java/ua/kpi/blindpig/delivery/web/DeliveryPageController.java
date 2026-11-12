package ua.kpi.blindpig.delivery.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.kpi.blindpig.delivery.dto.CourierDto;
import ua.kpi.blindpig.delivery.dto.SaveCourierRequest;
import ua.kpi.blindpig.delivery.service.CourierService;

@Controller
@RequestMapping("/deliveries")
public class DeliveryPageController {

    private final CourierService courierService;

    public DeliveryPageController(CourierService courierService) {
        this.courierService = courierService;
    }

    @GetMapping({"", "/info"})
    public String index(Model model) {
        model.addAttribute("message", "Система доставки та повідомлень ресторану The Blind Pig готова до роботи.");
        model.addAttribute("availableCouriersCount", courierService.findAvailable().size());
        return "delivery/index";
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
    public String toggleCourierAvailability(@PathVariable Long id) {
        CourierDto courier = courierService.getById(id);
        courierService.setAvailability(id, !courier.available());
        return "redirect:/deliveries/couriers";
    }
}
