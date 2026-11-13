package ua.kpi.blindpig.billing.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.kpi.blindpig.billing.dto.PaymentDto;
import ua.kpi.blindpig.billing.dto.PaymentOutcome;
import ua.kpi.blindpig.billing.dto.PaymentStatus;
import ua.kpi.blindpig.billing.service.PaymentService;
import ua.kpi.blindpig.billing.web.page.PaymentPageController;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PaymentPageController.class)
class PaymentPageControllerTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 11, 13, 12, 0);

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private PaymentService paymentService;

    private static PaymentDto payment(Long id, PaymentStatus status) {
        return new PaymentDto(id, 10L + id, new BigDecimal("216.00"), status, CREATED);
    }

    @Test
    void listShouldRenderAllOrFilteredPayments() throws Exception {
        when(paymentService.listByStatus(null)).thenReturn(List.of(payment(1L, PaymentStatus.PAID), payment(2L, PaymentStatus.FAILED)));
        when(paymentService.listByStatus(PaymentStatus.PENDING)).thenReturn(List.of());

        mvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(view().name("billing/payments"))
                .andExpect(content().string(containsString("оплачено")))
                .andExpect(content().string(containsString("відхилено")))
                .andExpect(content().string(containsString("216.00 грн")));

        mvc.perform(get("/payments").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedStatus", PaymentStatus.PENDING))
                .andExpect(content().string(containsString("Немає платежів зі статусом PENDING")));
    }

    @Test
    void invalidStatusFilterShouldRender400Page() throws Exception {
        mvc.perform(get("/payments").param("status", "LOST"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("billing/error"));
    }

    @Test
    void pendingDetailsShouldOfferSimulationButtons() throws Exception {
        when(paymentService.getById(1L)).thenReturn(payment(1L, PaymentStatus.PENDING));

        mvc.perform(get("/payments/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("очікує оплати")))
                .andExpect(content().string(containsString("Оплата успішна")))
                .andExpect(content().string(containsString("Оплата неуспішна")));
    }

    @Test
    void finishedDetailsShouldHideSimulationButtons() throws Exception {
        when(paymentService.getById(1L)).thenReturn(payment(1L, PaymentStatus.PAID));

        mvc.perform(get("/payments/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Оплата успішна"))));
    }

    @Test
    void unknownPaymentShouldRender404Page() throws Exception {
        when(paymentService.getById(99L)).thenThrow(new ResourceNotFoundException("Payment 99 not found"));

        mvc.perform(get("/payments/99")).andExpect(status().isNotFound()).andExpect(view().name("billing/error"));
    }

    @Test
    void createShouldRedirectToDetails() throws Exception {
        when(paymentService.createForOrder(11L)).thenReturn(payment(1L, PaymentStatus.PENDING));

        mvc.perform(post("/payments").param("orderId", "11"))
                .andExpect(redirectedUrl("/payments/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void createConflictShouldRedirectBackWithError() throws Exception {
        when(paymentService.createForOrder(11L)).thenThrow(new BusinessConflictException("Payment for order 11 already exists"));

        mvc.perform(post("/payments").param("orderId", "11"))
                .andExpect(redirectedUrl("/payments"))
                .andExpect(flash().attribute("error", "Payment for order 11 already exists"));
    }

    @Test
    void simulateShouldRedirectWithResult() throws Exception {
        when(paymentService.simulateResult(1L, PaymentOutcome.SUCCESS)).thenReturn(payment(1L, PaymentStatus.PAID));

        mvc.perform(post("/payments/1/simulate").param("outcome", "SUCCESS"))
                .andExpect(redirectedUrl("/payments/1"))
                .andExpect(flash().attributeExists("success"));
        verify(paymentService).simulateResult(1L, PaymentOutcome.SUCCESS);
    }

    @Test
    void repeatedSimulationShouldShowError() throws Exception {
        when(paymentService.simulateResult(1L, PaymentOutcome.FAILURE)).thenThrow(new BusinessConflictException("Payment 1 is already PAID"));

        mvc.perform(post("/payments/1/simulate").param("outcome", "FAILURE"))
                .andExpect(redirectedUrl("/payments/1"))
                .andExpect(flash().attribute("error", "Payment 1 is already PAID"));
    }
}
