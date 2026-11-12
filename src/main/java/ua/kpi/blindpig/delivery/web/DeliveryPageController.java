package ua.kpi.blindpig.delivery.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
