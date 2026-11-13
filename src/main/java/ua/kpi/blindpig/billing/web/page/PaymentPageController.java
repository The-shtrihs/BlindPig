package ua.kpi.blindpig.billing.web.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.kpi.blindpig.billing.dto.PaymentDto;
import ua.kpi.blindpig.billing.dto.PaymentOutcome;
import ua.kpi.blindpig.billing.dto.PaymentStatus;
import ua.kpi.blindpig.billing.service.PaymentService;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

@Controller
@RequestMapping("/payments")
public class PaymentPageController {

    private final PaymentService paymentService;

    public PaymentPageController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) PaymentStatus status, Model model) {
        model.addAttribute("payments", paymentService.listByStatus(status));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", PaymentStatus.values());
        return "billing/payments";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("payment", paymentService.getById(id));
        model.addAttribute("outcomes", PaymentOutcome.values());
        return "billing/payment-details";
    }

    @PostMapping
    public String create(@RequestParam(required = false) Long orderId, RedirectAttributes redirect) {
        try {
            PaymentDto payment = paymentService.createForOrder(orderId);
            redirect.addFlashAttribute("success", "Платіж #" + payment.id() + " для замовлення #" + orderId + " створено");
            return "redirect:/payments/" + payment.id();
        } catch (InvalidRequestException | ResourceNotFoundException | BusinessConflictException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/payments";
        }
    }

    @PostMapping("/{id}/simulate")
    public String simulate(@PathVariable Long id, @RequestParam PaymentOutcome outcome, RedirectAttributes redirect) {
        try {
            PaymentDto payment = paymentService.simulateResult(id, outcome);
            redirect.addFlashAttribute("success", "Платіж #" + id + " → " + payment.status());
        } catch (BusinessConflictException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/payments/" + id;
    }
}
