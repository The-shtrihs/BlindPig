package ua.kpi.blindpig.billing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.billing.dto.PromoCodeDto;
import ua.kpi.blindpig.billing.dto.PromoQuote;
import ua.kpi.blindpig.billing.dto.SavePromoCodeRequest;
import ua.kpi.blindpig.billing.repository.inmemory.InMemoryPromoCodeRepository;
import ua.kpi.blindpig.billing.service.PromoCodeService;
import ua.kpi.blindpig.billing.service.impl.PromoCodeServiceImpl;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromoCodeServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 13, 12, 0);

    private PromoCodeService service;

    @BeforeEach
    void setUp() {
        service = new PromoCodeServiceImpl(new InMemoryPromoCodeRepository());
    }

    private static SavePromoCodeRequest percent(String code, String value) {
        return new SavePromoCodeRequest(code, DiscountType.PERCENTAGE, new BigDecimal(value), null, null, null, true);
    }

    @Test
    void shouldNormalizeCodeAndFindCaseInsensitively() {
        PromoCodeDto created = service.create(percent("  welcome10 ", "10"));

        assertThat(created.code()).isEqualTo("WELCOME10");
        assertThat(service.getByCode("Welcome10").id()).isEqualTo(created.id());
        assertThat(service.getById(created.id()).value()).isEqualByComparingTo("10");
    }

    @Test
    void shouldRejectDuplicateCodeIgnoringCase() {
        service.create(percent("WELCOME10", "10"));
        assertThatThrownBy(() -> service.create(percent("welcome10", "5"))).isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void updateShouldAllowSameCodeButRejectOtherExistingCode() {
        PromoCodeDto a = service.create(percent("AAA", "10"));
        service.create(percent("BBB", "10"));

        assertThat(service.update(a.id(), percent("aaa", "15")).value()).isEqualByComparingTo("15");
        assertThatThrownBy(() -> service.update(a.id(), percent("BBB", "15"))).isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void shouldRejectInvalidRequests() {
        assertThatThrownBy(() -> service.create(percent("ab", "10"))).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.create(percent("BAD CODE", "10"))).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.create(percent("OK1", "101"))).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.create(new SavePromoCodeRequest("OK1", DiscountType.FIXED, BigDecimal.TEN,
                new BigDecimal("-1"), null, null, true))).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.create(new SavePromoCodeRequest("OK1", DiscountType.FIXED, BigDecimal.TEN,
                null, NOW, NOW.minusDays(1), true))).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldThrowNotFound() {
        assertThatThrownBy(() -> service.getById(42L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.getByCode("NOPE")).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete(42L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteShouldRemoveCode() {
        PromoCodeDto created = service.create(percent("GONE", "10"));
        service.delete(created.id());
        assertThat(service.list()).isEmpty();
    }

    @Test
    void applyPercentagePromoAfterAutomaticDiscounts() {
        service.create(percent("WELCOME10", "10"));

        PromoQuote quote = service.applyPromo("welcome10", new BigDecimal("240.00"), NOW);

        assertThat(quote.code()).isEqualTo("WELCOME10");
        assertThat(quote.discountAmount()).isEqualTo(new BigDecimal("24.00"));
    }

    @Test
    void fixedPromoShouldNotExceedSubtotal() {
        service.create(new SavePromoCodeRequest("MINUS50", DiscountType.FIXED, new BigDecimal("50"), null, null, null, true));

        assertThat(service.applyPromo("MINUS50", new BigDecimal("30"), NOW).discountAmount()).isEqualTo(new BigDecimal("30.00"));
    }

    @Test
    void applyShouldRejectUnknownInactiveExpiredOrBelowMinimumWith409() {
        service.create(new SavePromoCodeRequest("OFF", DiscountType.PERCENTAGE, BigDecimal.TEN, null, null, null, false));
        service.create(new SavePromoCodeRequest("OLD", DiscountType.PERCENTAGE, BigDecimal.TEN, null, null, NOW, true));
        service.create(new SavePromoCodeRequest("SOON", DiscountType.PERCENTAGE, BigDecimal.TEN, null, NOW.plusDays(1), null, true));
        service.create(new SavePromoCodeRequest("BIG", DiscountType.FIXED, BigDecimal.TEN, new BigDecimal("300"), null, null, true));

        assertThatThrownBy(() -> service.applyPromo("NOPE", BigDecimal.TEN, NOW)).isInstanceOf(BusinessConflictException.class);
        assertThatThrownBy(() -> service.applyPromo("OFF", BigDecimal.TEN, NOW)).isInstanceOf(BusinessConflictException.class);
        assertThatThrownBy(() -> service.applyPromo("OLD", BigDecimal.TEN, NOW)).isInstanceOf(BusinessConflictException.class);
        assertThatThrownBy(() -> service.applyPromo("SOON", BigDecimal.TEN, NOW)).isInstanceOf(BusinessConflictException.class);
        assertThatThrownBy(() -> service.applyPromo("BIG", new BigDecimal("299.99"), NOW)).isInstanceOf(BusinessConflictException.class);
        assertThat(service.applyPromo("BIG", new BigDecimal("300"), NOW).discountAmount()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    void applyShouldRejectBadInputWith400() {
        assertThatThrownBy(() -> service.applyPromo(" ", BigDecimal.TEN, NOW)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.applyPromo("X", new BigDecimal("-1"), NOW)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.applyPromo("X", BigDecimal.TEN, null)).isInstanceOf(InvalidRequestException.class);
    }
}
