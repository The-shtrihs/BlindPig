package ua.kpi.blindpig.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.kpi.blindpig.order.dto.FoodOrderDto;
import ua.kpi.blindpig.order.dto.OrderStatus;
import ua.kpi.blindpig.order.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full application context with the order module mocked (until Даня's OrderService is merged):
 * real billing services + in-memory repositories + seed data + OpenAPI.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BillingIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private OrderService orderService;

    @Test
    void seededPromotionsGive300To240To216() throws Exception {
        mvc.perform(get("/api/discounts/best").param("menuItemId", "1").param("categoryId", "1").param("basePrice", "300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalUnitPrice").value(240.00));
        mvc.perform(post("/api/promo-codes/apply").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"welcome10\",\"subtotal\":240}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountAmount").value(24.00));
    }

    @Test
    void paymentFlowConfirmsOrderOnSuccess() throws Exception {
        when(orderService.getById(100L)).thenReturn(new FoodOrderDto(100L, 1L, "Kyiv", OrderStatus.CREATED, List.of(),
                new BigDecimal("300.00"), new BigDecimal("60.00"), new BigDecimal("24.00"), new BigDecimal("216.00"), LocalDateTime.now()));

        String location = mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content("{\"orderId\":100}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(216.00))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(post(location + "/simulate").contentType(MediaType.APPLICATION_JSON).content("{\"outcome\":\"SUCCESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
        verify(orderService).confirmPaidOrder(100L);

        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content("{\"orderId\":100}"))
                .andExpect(status().isConflict());
    }

    @Test
    void htmlPagesRenderWithRealServices() throws Exception {
        mvc.perform(get("/promotions")).andExpect(status().isOk());
        mvc.perform(get("/promotions/calculator")).andExpect(status().isOk());
        mvc.perform(get("/payments")).andExpect(status().isOk());
        mvc.perform(get("/css/billing.css")).andExpect(status().isOk());
    }

    @Test
    void openApiDocumentsBillingEndpoints() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/discounts']").exists())
                .andExpect(jsonPath("$.paths['/api/discounts/best'].get").exists())
                .andExpect(jsonPath("$.paths['/api/promo-codes/apply'].post").exists())
                .andExpect(jsonPath("$.paths['/api/payments/{id}/simulate'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/discounts'].post.responses['201']").exists())
                .andExpect(jsonPath("$.tags[*].name", hasItem("Payments")))
                .andExpect(jsonPath("$.components.schemas.SavePromoCodeRequest.properties.code.example").value("WELCOME10"));
    }
}
