package ua.kpi.blindpig.billing.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.kpi.blindpig.billing.dto.PaymentDto;
import ua.kpi.blindpig.billing.dto.PaymentOutcome;
import ua.kpi.blindpig.billing.dto.PaymentStatus;
import ua.kpi.blindpig.billing.service.PaymentService;
import ua.kpi.blindpig.billing.web.api.PaymentRestController;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentRestController.class)
class PaymentRestControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private PaymentService paymentService;

    private static PaymentDto payment(PaymentStatus status) {
        return new PaymentDto(5L, 11L, new BigDecimal("216.00"), status, LocalDateTime.of(2026, 11, 13, 12, 0));
    }

    @Test
    void createShouldReturn201WithLocation() throws Exception {
        when(paymentService.createForOrder(11L)).thenReturn(payment(PaymentStatus.PENDING));

        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content("{\"orderId\":11}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/payments/5")))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(216.00))
                .andExpect(jsonPath("$.createdAt").value("2026-11-13T12:00:00"));
    }

    @Test
    void createWithoutOrderIdShouldReturn400() throws Exception {
        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("orderId: Order id is required"));
        verifyNoInteractions(paymentService);
    }

    @Test
    void createForUnknownOrderShould404AndDuplicateShould409() throws Exception {
        when(paymentService.createForOrder(404L)).thenThrow(new ResourceNotFoundException("Order 404 not found"));
        when(paymentService.createForOrder(11L)).thenThrow(new BusinessConflictException("Payment for order 11 already exists"));

        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content("{\"orderId\":404}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content("{\"orderId\":11}"))
                .andExpect(status().isConflict());
    }

    @Test
    void listFilterByStatusAndInvalidStatus() throws Exception {
        when(paymentService.listByStatus(PaymentStatus.PAID)).thenReturn(List.of(payment(PaymentStatus.PAID)));
        when(paymentService.listByStatus(null)).thenReturn(List.of());

        mvc.perform(get("/api/payments").param("status", "PAID")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/payments")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/payments").param("status", "LOST")).andExpect(status().isBadRequest());
    }

    @Test
    void getByIdAndByOrder() throws Exception {
        when(paymentService.getById(5L)).thenReturn(payment(PaymentStatus.PENDING));
        when(paymentService.getByOrder(12L)).thenThrow(new ResourceNotFoundException("Payment for order 12 not found"));

        mvc.perform(get("/api/payments/5")).andExpect(status().isOk()).andExpect(jsonPath("$.orderId").value(11));
        mvc.perform(get("/api/payments/by-order/12")).andExpect(status().isNotFound());
    }

    @Test
    void simulateShouldReturnNewStateAndSecondAttemptShould409() throws Exception {
        when(paymentService.simulateResult(5L, PaymentOutcome.SUCCESS))
                .thenReturn(payment(PaymentStatus.PAID))
                .thenThrow(new BusinessConflictException("Payment 5 is already PAID"));

        mvc.perform(post("/api/payments/5/simulate").contentType(MediaType.APPLICATION_JSON).content("{\"outcome\":\"SUCCESS\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAID"));
        mvc.perform(post("/api/payments/5/simulate").contentType(MediaType.APPLICATION_JSON).content("{\"outcome\":\"SUCCESS\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("Payment 5 is already PAID"));
        mvc.perform(post("/api/payments/5/simulate").contentType(MediaType.APPLICATION_JSON).content("{\"outcome\":\"MAYBE\"}"))
                .andExpect(status().isBadRequest());
    }
}
