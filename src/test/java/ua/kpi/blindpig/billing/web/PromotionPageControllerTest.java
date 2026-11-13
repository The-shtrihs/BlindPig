package ua.kpi.blindpig.billing.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.kpi.blindpig.billing.dto.DiscountDto;
import ua.kpi.blindpig.billing.dto.DiscountQuote;
import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.billing.dto.PromoCodeDto;
import ua.kpi.blindpig.billing.dto.PromoQuote;
import ua.kpi.blindpig.billing.dto.SaveDiscountRequest;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.billing.service.PromoCodeService;
import ua.kpi.blindpig.billing.web.page.PromotionPageController;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(PromotionPageController.class)
class PromotionPageControllerTest {

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
    private DiscountService discountService;
    @MockitoBean
    private PromoCodeService promoCodeService;

    @Test
    void listShouldRenderDiscountsAndPromoCodesWithStatuses() throws Exception {
        when(discountService.list()).thenReturn(List.of(
                new DiscountDto(1L, "Піца <b>−20%</b>", DiscountType.PERCENTAGE, new BigDecimal("20.00"), 1L, null, null, null, true),
                new DiscountDto(2L, "Стара", DiscountType.FIXED, new BigDecimal("50.00"), null, 3L, null, NOW.minusDays(1), true)));
        when(promoCodeService.list()).thenReturn(List.of(
                new PromoCodeDto(5L, "WELCOME10", DiscountType.PERCENTAGE, new BigDecimal("10.00"), null, NOW.plusDays(1), null, true)));

        mvc.perform(get("/promotions"))
                .andExpect(status().isOk())
                .andExpect(view().name("billing/promotions"))
                .andExpect(model().attributeExists("discounts", "promoCodes"))
                .andExpect(content().string(containsString("Піца &lt;b&gt;−20%&lt;/b&gt;")))   // escaped by th:text
                .andExpect(content().string(not(containsString("<b>−20%</b>"))))
                .andExpect(content().string(containsString("Категорія #3")))
                .andExpect(content().string(containsString("завершилась")))
                .andExpect(content().string(containsString("ще не почалась")))
                .andExpect(content().string(containsString("WELCOME10")));
    }

    @Test
    void emptyListShouldShowPlaceholders() throws Exception {
        when(discountService.list()).thenReturn(List.of());
        when(promoCodeService.list()).thenReturn(List.of());

        mvc.perform(get("/promotions"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Знижок поки немає")))
                .andExpect(content().string(containsString("Промокодів поки немає")));
    }

    @Test
    void newAndEditFormsShouldRender() throws Exception {
        mvc.perform(get("/promotions/discounts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("billing/discount-form"));

        when(discountService.getById(1L)).thenReturn(
                new DiscountDto(1L, "Піца", DiscountType.PERCENTAGE, new BigDecimal("20.00"), 1L, null, NOW, null, true));
        mvc.perform(get("/promotions/discounts/1/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"2026-11-13T12:00\"")));

        mvc.perform(get("/promotions/promo-codes/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("billing/promo-code-form"));
    }

    @Test
    void createDiscountShouldRedirectWithFlash() throws Exception {
        when(discountService.create(any())).thenReturn(
                new DiscountDto(9L, "Суші", DiscountType.PERCENTAGE, new BigDecimal("15.00"), null, 2L, null, null, true));

        mvc.perform(post("/promotions/discounts")
                        .param("name", "Суші").param("type", "PERCENTAGE").param("value", "15")
                        .param("categoryId", "2").param("validFrom", "2026-11-13T10:00").param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/promotions"))
                .andExpect(flash().attributeExists("success"));

        verify(discountService).create(new SaveDiscountRequest("Суші", DiscountType.PERCENTAGE, new BigDecimal("15"),
                null, 2L, LocalDateTime.of(2026, 11, 13, 10, 0), null, true));
    }

    @Test
    void serviceValidationErrorShouldRerenderForm() throws Exception {
        when(discountService.update(eq(1L), any())).thenThrow(new InvalidRequestException("Discount must target exactly one of menuItemId or categoryId"));

        mvc.perform(post("/promotions/discounts/1")
                        .param("name", "X").param("type", "FIXED").param("value", "5")
                        .param("menuItemId", "1").param("categoryId", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("billing/discount-form"))
                .andExpect(content().string(containsString("exactly one of menuItemId or categoryId")));
    }

    @Test
    void bindingErrorShouldRerenderForm() throws Exception {
        mvc.perform(post("/promotions/promo-codes").param("code", "OK1").param("type", "FIXED").param("value", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("billing/promo-code-form"))
                .andExpect(model().attributeHasFieldErrors("promoCodeForm", "value"));
    }

    @Test
    void duplicatePromoCodeShouldRerenderForm() throws Exception {
        when(promoCodeService.create(any())).thenThrow(new BusinessConflictException("Promo code WELCOME10 already exists"));

        mvc.perform(post("/promotions/promo-codes").param("code", "welcome10").param("type", "PERCENTAGE").param("value", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("already exists")));
    }

    @Test
    void deleteShouldRedirect() throws Exception {
        mvc.perform(post("/promotions/promo-codes/3/delete"))
                .andExpect(redirectedUrl("/promotions"))
                .andExpect(flash().attributeExists("success"));
        verify(promoCodeService).delete(3L);
    }

    @Test
    void unknownIdShouldRender404Page() throws Exception {
        when(discountService.getById(anyLong())).thenThrow(new ResourceNotFoundException("Discount 99 not found"));

        mvc.perform(get("/promotions/discounts/99/edit"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("billing/error"))
                .andExpect(content().string(containsString("Discount 99 not found")));
    }

    @Test
    void calculatorShouldShowBreakdown300To216() throws Exception {
        when(discountService.calculateBestDiscount(eq(1L), eq(1L), any(), eq(NOW))).thenReturn(
                new DiscountQuote(4L, new BigDecimal("300.00"), new BigDecimal("60.00"), new BigDecimal("240.00")));
        when(promoCodeService.applyPromo(eq("WELCOME10"), any(), eq(NOW)))
                .thenReturn(new PromoQuote(1L, "WELCOME10", new BigDecimal("24.00")));

        mvc.perform(get("/promotions/calculator")).andExpect(status().isOk());

        mvc.perform(post("/promotions/calculator")
                        .param("menuItemId", "1").param("categoryId", "1").param("basePrice", "300")
                        .param("quantity", "1").param("promoCode", "WELCOME10"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("preview"))
                .andExpect(content().string(containsString("240.00 грн")))
                .andExpect(content().string(containsString("−24.00 грн")))
                .andExpect(content().string(containsString("216.00 грн")));
    }

    @Test
    void calculatorShouldShowRejectedPromoAndKeepTotal() throws Exception {
        when(discountService.calculateBestDiscount(any(), any(), any(), any())).thenReturn(
                new DiscountQuote(null, new BigDecimal("100.00"), new BigDecimal("0.00"), new BigDecimal("100.00")));
        when(promoCodeService.applyPromo(any(), any(), any())).thenThrow(new BusinessConflictException("Promo code OLD is not active"));

        mvc.perform(post("/promotions/calculator")
                        .param("menuItemId", "1").param("basePrice", "50").param("quantity", "2").param("promoCode", "old"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Promo code OLD is not active")))
                .andExpect(content().string(containsString("200.00 грн")));
    }

    @Test
    void calculatorShouldRejectNonPositiveQuantity() throws Exception {
        mvc.perform(post("/promotions/calculator").param("menuItemId", "1").param("basePrice", "50").param("quantity", "0"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("calculatorForm", "quantity"));
    }
}
