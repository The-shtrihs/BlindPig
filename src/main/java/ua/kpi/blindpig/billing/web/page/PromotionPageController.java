package ua.kpi.blindpig.billing.web.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.kpi.blindpig.billing.dto.DiscountDto;
import ua.kpi.blindpig.billing.dto.DiscountQuote;
import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.billing.dto.PromoCodeDto;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.billing.service.PromoCodeService;
import ua.kpi.blindpig.billing.web.page.form.DiscountForm;
import ua.kpi.blindpig.billing.web.page.form.PriceCalculatorForm;
import ua.kpi.blindpig.billing.web.page.form.PromoCodeForm;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/promotions")
public class PromotionPageController {

    public record DiscountRow(DiscountDto discount, PromotionStatus status) {
    }

    public record PromoCodeRow(PromoCodeDto promoCode, PromotionStatus status) {
    }

    private final DiscountService discountService;
    private final PromoCodeService promoCodeService;
    private final Clock clock;

    public PromotionPageController(DiscountService discountService, PromoCodeService promoCodeService, Clock clock) {
        this.discountService = discountService;
        this.promoCodeService = promoCodeService;
        this.clock = clock;
    }

    @ModelAttribute("discountTypes")
    DiscountType[] discountTypes() {
        return DiscountType.values();
    }

    @GetMapping
    public String list(Model model) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<DiscountRow> discounts = discountService.list().stream()
                .map(d -> new DiscountRow(d, PromotionStatus.of(d.active(), d.validFrom(), d.validUntil(), now)))
                .toList();
        List<PromoCodeRow> promoCodes = promoCodeService.list().stream()
                .map(p -> new PromoCodeRow(p, PromotionStatus.of(p.active(), p.validFrom(), p.validUntil(), now)))
                .toList();
        model.addAttribute("discounts", discounts);
        model.addAttribute("promoCodes", promoCodes);
        model.addAttribute("now", now);
        return "billing/promotions";
    }

    // ---- discounts ----

    @GetMapping("/discounts/new")
    public String newDiscount(Model model) {
        model.addAttribute("discountForm", new DiscountForm());
        model.addAttribute("discountId", null);
        return "billing/discount-form";
    }

    @GetMapping("/discounts/{id}/edit")
    public String editDiscount(@PathVariable Long id, Model model) {
        model.addAttribute("discountForm", DiscountForm.from(discountService.getById(id)));
        model.addAttribute("discountId", id);
        return "billing/discount-form";
    }

    @PostMapping("/discounts")
    public String createDiscount(@ModelAttribute("discountForm") DiscountForm form, BindingResult binding,
                                 Model model, RedirectAttributes redirect) {
        return saveDiscount(null, form, binding, model, redirect);
    }

    @PostMapping("/discounts/{id}")
    public String updateDiscount(@PathVariable Long id, @ModelAttribute("discountForm") DiscountForm form,
                                 BindingResult binding, Model model, RedirectAttributes redirect) {
        return saveDiscount(id, form, binding, model, redirect);
    }

    @PostMapping("/discounts/{id}/delete")
    public String deleteDiscount(@PathVariable Long id, RedirectAttributes redirect) {
        discountService.delete(id);
        redirect.addFlashAttribute("success", "Знижку #" + id + " видалено");
        return "redirect:/promotions";
    }

    private String saveDiscount(Long id, DiscountForm form, BindingResult binding, Model model, RedirectAttributes redirect) {
        model.addAttribute("discountId", id);
        if (binding.hasErrors()) {
            return "billing/discount-form";
        }
        try {
            DiscountDto saved = id == null
                    ? discountService.create(form.toRequest())
                    : discountService.update(id, form.toRequest());
            redirect.addFlashAttribute("success", "Знижку «" + saved.name() + "» збережено");
            return "redirect:/promotions";
        } catch (InvalidRequestException | BusinessConflictException e) {
            binding.reject("billing.invalid", e.getMessage());
            return "billing/discount-form";
        }
    }

    // ---- promo codes ----

    @GetMapping("/promo-codes/new")
    public String newPromoCode(Model model) {
        model.addAttribute("promoCodeForm", new PromoCodeForm());
        model.addAttribute("promoCodeId", null);
        return "billing/promo-code-form";
    }

    @GetMapping("/promo-codes/{id}/edit")
    public String editPromoCode(@PathVariable Long id, Model model) {
        model.addAttribute("promoCodeForm", PromoCodeForm.from(promoCodeService.getById(id)));
        model.addAttribute("promoCodeId", id);
        return "billing/promo-code-form";
    }

    @PostMapping("/promo-codes")
    public String createPromoCode(@ModelAttribute("promoCodeForm") PromoCodeForm form, BindingResult binding,
                                  Model model, RedirectAttributes redirect) {
        return savePromoCode(null, form, binding, model, redirect);
    }

    @PostMapping("/promo-codes/{id}")
    public String updatePromoCode(@PathVariable Long id, @ModelAttribute("promoCodeForm") PromoCodeForm form,
                                  BindingResult binding, Model model, RedirectAttributes redirect) {
        return savePromoCode(id, form, binding, model, redirect);
    }

    @PostMapping("/promo-codes/{id}/delete")
    public String deletePromoCode(@PathVariable Long id, RedirectAttributes redirect) {
        promoCodeService.delete(id);
        redirect.addFlashAttribute("success", "Промокод #" + id + " видалено");
        return "redirect:/promotions";
    }

    private String savePromoCode(Long id, PromoCodeForm form, BindingResult binding, Model model, RedirectAttributes redirect) {
        model.addAttribute("promoCodeId", id);
        if (binding.hasErrors()) {
            return "billing/promo-code-form";
        }
        try {
            PromoCodeDto saved = id == null
                    ? promoCodeService.create(form.toRequest())
                    : promoCodeService.update(id, form.toRequest());
            redirect.addFlashAttribute("success", "Промокод " + saved.code() + " збережено");
            return "redirect:/promotions";
        } catch (InvalidRequestException | BusinessConflictException e) {
            binding.reject("billing.invalid", e.getMessage());
            return "billing/promo-code-form";
        }
    }

    // ---- calculator ----

    @GetMapping("/calculator")
    public String calculatorForm(Model model) {
        model.addAttribute("calculatorForm", new PriceCalculatorForm());
        return "billing/calculator";
    }

    @PostMapping("/calculator")
    public String calculate(@ModelAttribute("calculatorForm") PriceCalculatorForm form, BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            return "billing/calculator";
        }
        if (form.getQuantity() <= 0) {
            binding.rejectValue("quantity", "billing.quantity", "Кількість має бути більшою за 0");
            return "billing/calculator";
        }
        try {
            model.addAttribute("preview", preview(form, LocalDateTime.now(clock)));
        } catch (InvalidRequestException e) {
            binding.reject("billing.invalid", e.getMessage());
        }
        return "billing/calculator";
    }

    private PricePreview preview(PriceCalculatorForm form, LocalDateTime at) {
        DiscountQuote quote = discountService.calculateBestDiscount(form.getMenuItemId(), form.getCategoryId(), form.getBasePrice(), at);
        BigDecimal qty = BigDecimal.valueOf(form.getQuantity());
        BigDecimal subtotal = quote.baseUnitPrice().multiply(qty);
        BigDecimal automaticDiscount = quote.discountPerUnit().multiply(qty);
        BigDecimal subtotalAfterAuto = quote.finalUnitPrice().multiply(qty);

        String code = form.getPromoCode() == null ? "" : form.getPromoCode().trim();
        BigDecimal promoDiscount = BigDecimal.ZERO.setScale(2);
        String promoError = null;
        if (!code.isEmpty()) {
            try {
                promoDiscount = promoCodeService.applyPromo(code, subtotalAfterAuto, at).discountAmount();
            } catch (BusinessConflictException e) {
                promoError = e.getMessage();
            }
        }
        BigDecimal total = subtotalAfterAuto.subtract(promoDiscount).max(BigDecimal.ZERO.setScale(2));
        return new PricePreview(quote, form.getQuantity(), subtotal, automaticDiscount, subtotalAfterAuto,
                code.isEmpty() ? null : code, promoDiscount, promoError, total);
    }
}
