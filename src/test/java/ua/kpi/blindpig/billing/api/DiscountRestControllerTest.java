package ua.kpi.blindpig.billing.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.kpi.blindpig.billing.dto.DiscountDto;
import ua.kpi.blindpig.billing.dto.DiscountQuote;
import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.billing.web.api.DiscountRestController;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiscountRestController.class)
class DiscountRestControllerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 13, 12, 0);
    private static final String VALID = """
            {"name":"Піца -20%","type":"PERCENTAGE","value":20,"menuItemId":1,"active":true}""";

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

    private static DiscountDto dto(Long id) {
        return new DiscountDto(id, "Піца -20%", DiscountType.PERCENTAGE, new BigDecimal("20.00"), 1L, null, null, null, true);
    }

    @Test
    void listAllOrActiveAt() throws Exception {
        when(discountService.list()).thenReturn(List.of(dto(1L), dto(2L)));
        when(discountService.findActive(LocalDateTime.of(2026, 11, 13, 10, 0))).thenReturn(List.of(dto(1L)));

        mvc.perform(get("/api/discounts")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/discounts").param("activeAt", "2026-11-13T10:00:00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createShouldReturn201WithLocation() throws Exception {
        when(discountService.create(any())).thenReturn(dto(7L));

        mvc.perform(post("/api/discounts").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/discounts/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.type").value("PERCENTAGE"))
                .andExpect(jsonPath("$.value").value(20.00));
    }

    @Test
    void beanValidationShouldReturn400WithDetails() throws Exception {
        mvc.perform(post("/api/discounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"value\":-5,\"menuItemId\":1,\"active\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details", hasItem("name: Discount name must not be blank")))
                .andExpect(jsonPath("$.details", hasItem("type: Discount type is required")))
                .andExpect(jsonPath("$.details", hasItem("value: Discount value must be greater than 0")));
        verifyNoInteractions(discountService);
    }

    @Test
    void serviceRuleViolationShouldReturn400() throws Exception {
        when(discountService.create(any())).thenThrow(new InvalidRequestException("Percentage discount must be in range (0, 100]"));

        mvc.perform(post("/api/discounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"type\":\"PERCENTAGE\",\"value\":150,\"menuItemId\":1,\"active\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Percentage discount must be in range (0, 100]"));
    }

    @Test
    void unknownEnumOrMalformedJsonShouldReturn400() throws Exception {
        mvc.perform(post("/api/discounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"type\":\"HALF_PRICE\",\"value\":5,\"menuItemId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or missing value for field 'type'"));
        mvc.perform(post("/api/discounts").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Malformed JSON")));
    }

    @Test
    void missingPrimitiveActiveShouldReturn400NamingTheField() throws Exception {
        mvc.perform(post("/api/discounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"type\":\"FIXED\",\"value\":5,\"menuItemId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or missing value for field 'active'"));
        verifyNoInteractions(discountService);
    }

    @Test
    void getUpdateDeleteUnknownShouldReturn404() throws Exception {
        when(discountService.getById(anyLong())).thenThrow(new ResourceNotFoundException("Discount 99 not found"));
        when(discountService.update(eq(99L), any())).thenThrow(new ResourceNotFoundException("Discount 99 not found"));
        doThrow(new ResourceNotFoundException("Discount 99 not found")).when(discountService).delete(99L);

        mvc.perform(get("/api/discounts/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Discount 99 not found"));
        mvc.perform(put("/api/discounts/99").contentType(MediaType.APPLICATION_JSON).content(VALID)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/discounts/99")).andExpect(status().isNotFound());
    }

    @Test
    void updateShouldReturn200AndDeleteShouldReturn204() throws Exception {
        when(discountService.update(eq(1L), any())).thenReturn(dto(1L));

        mvc.perform(put("/api/discounts/1").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        mvc.perform(delete("/api/discounts/1")).andExpect(status().isNoContent());
        verify(discountService).delete(1L);
    }

    @Test
    void bestShouldDefaultToNowAndReturnQuote() throws Exception {
        when(discountService.calculateBestDiscount(1L, 2L, new BigDecimal("300"), NOW))
                .thenReturn(new DiscountQuote(4L, new BigDecimal("300.00"), new BigDecimal("60.00"), new BigDecimal("240.00")));

        mvc.perform(get("/api/discounts/best").param("menuItemId", "1").param("categoryId", "2").param("basePrice", "300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountId").value(4))
                .andExpect(jsonPath("$.finalUnitPrice").value(240.00));
    }

    @Test
    void bestWithMissingOrBadParamsShouldReturn400() throws Exception {
        mvc.perform(get("/api/discounts/best").param("menuItemId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required parameter 'basePrice'"));
        mvc.perform(get("/api/discounts/best").param("menuItemId", "abc").param("basePrice", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'menuItemId'"));
    }
}
