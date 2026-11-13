package ua.kpi.blindpig.billing.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.billing.dto.PromoCodeDto;
import ua.kpi.blindpig.billing.dto.PromoQuote;
import ua.kpi.blindpig.billing.service.PromoCodeService;
import ua.kpi.blindpig.billing.web.api.PromoCodeRestController;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromoCodeRestController.class)
class PromoCodeRestControllerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 13, 12, 0);

    @TestConfiguration
    static class FixedClock {
        @Bean
        Clock billingClock() {
            return Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private PromoCodeService promoCodeService;

    private static PromoCodeDto dto(Long id) {
        return new PromoCodeDto(id, "WELCOME10", DiscountType.PERCENTAGE, new BigDecimal("10.00"), null, null, null, true);
    }

    @Test
    void createShouldReturn201AndDuplicateShould409() throws Exception {
        String body = "{\"code\":\"welcome10\",\"type\":\"PERCENTAGE\",\"value\":10,\"active\":true}";
        when(promoCodeService.create(any())).thenReturn(dto(3L))
                .thenThrow(new BusinessConflictException("Promo code WELCOME10 already exists"));

        mvc.perform(post("/api/promo-codes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/promo-codes/3")))
                .andExpect(jsonPath("$.code").value("WELCOME10"));
        mvc.perform(post("/api/promo-codes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Promo code WELCOME10 already exists"));
    }

    @Test
    void invalidCodeFormatShouldReturn400() throws Exception {
        mvc.perform(post("/api/promo-codes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"no spaces!\",\"type\":\"FIXED\",\"value\":10,\"minOrderAmount\":-1,\"active\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem("code: Promo code must be 3-32 characters: letters, digits, '-' or '_'")))
                .andExpect(jsonPath("$.details", hasItem("minOrderAmount: Minimum order amount must not be negative")));
    }

    @Test
    void getByIdAndCode() throws Exception {
        when(promoCodeService.getById(3L)).thenReturn(dto(3L));
        when(promoCodeService.getByCode("welcome10")).thenReturn(dto(3L));
        when(promoCodeService.getByCode("NOPE")).thenThrow(new ResourceNotFoundException("Promo code NOPE not found"));

        mvc.perform(get("/api/promo-codes/3")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(3));
        mvc.perform(get("/api/promo-codes/code/welcome10")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("WELCOME10"));
        mvc.perform(get("/api/promo-codes/code/NOPE")).andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        mvc.perform(delete("/api/promo-codes/3")).andExpect(status().isNoContent());
    }

    @Test
    void applyShouldReturnQuoteUsingNowByDefault() throws Exception {
        when(promoCodeService.applyPromo("WELCOME10", new BigDecimal("240.00"), NOW))
                .thenReturn(new PromoQuote(3L, "WELCOME10", new BigDecimal("24.00")));

        mvc.perform(post("/api/promo-codes/apply").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"WELCOME10\",\"subtotal\":240.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promoCodeId").value(3))
                .andExpect(jsonPath("$.discountAmount").value(24.00));
    }

    @Test
    void applyInvalidPromoShouldReturn409AndBadBodyShould400() throws Exception {
        when(promoCodeService.applyPromo(eq("OLD"), any(), eq(LocalDateTime.of(2026, 1, 1, 0, 0))))
                .thenThrow(new BusinessConflictException("Promo code OLD is not active"));

        mvc.perform(post("/api/promo-codes/apply").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"OLD\",\"subtotal\":100,\"at\":\"2026-01-01T00:00:00\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Promo code OLD is not active"));
        mvc.perform(post("/api/promo-codes/apply").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\",\"subtotal\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem("subtotal: Subtotal must not be negative")));
    }
}
