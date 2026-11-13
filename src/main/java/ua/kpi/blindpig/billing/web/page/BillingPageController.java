package ua.kpi.blindpig.billing.web.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.billing.service.PromoCodeService;

/** Lab 2: minimal MVC controller over billing services. */
@Controller
public class BillingPageController {

    private final DiscountService discountService;
    private final PromoCodeService promoCodeService;

    public BillingPageController(DiscountService discountService, PromoCodeService promoCodeService) {
        this.discountService = discountService;
        this.promoCodeService = promoCodeService;
    }

    @GetMapping("/promotions")
    public String promotions(Model model) {
        model.addAttribute("discounts", discountService.list());
        model.addAttribute("promoCodes", promoCodeService.list());
        return "billing/promotions";
    }
}
