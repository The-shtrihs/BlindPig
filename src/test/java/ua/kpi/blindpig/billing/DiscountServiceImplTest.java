package ua.kpi.blindpig.billing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.kpi.blindpig.billing.dto.DiscountDto;
import ua.kpi.blindpig.billing.dto.DiscountQuote;
import ua.kpi.blindpig.billing.dto.DiscountType;
import ua.kpi.blindpig.billing.dto.SaveDiscountRequest;
import ua.kpi.blindpig.billing.repository.inmemory.InMemoryDiscountRepository;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.billing.service.impl.DiscountServiceImpl;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 13, 12, 0);

    private DiscountService service;

    @BeforeEach
    void setUp() {
        service = new DiscountServiceImpl(new InMemoryDiscountRepository());
    }

    private static SaveDiscountRequest itemPercent(String value, Long menuItemId) {
        return new SaveDiscountRequest("Item", DiscountType.PERCENTAGE, new BigDecimal(value), menuItemId, null, null, null, true);
    }

    private static SaveDiscountRequest categoryPercent(String value, Long categoryId) {
        return new SaveDiscountRequest("Category", DiscountType.PERCENTAGE, new BigDecimal(value), null, categoryId, null, null, true);
    }

    @Test
    void shouldCreateReadUpdateAndDelete() {
        DiscountDto created = service.create(itemPercent("20", 1L));
        assertThat(created.id()).isNotNull();
        assertThat(created.value()).isEqualByComparingTo("20");
        assertThat(service.getById(created.id()).name()).isEqualTo("Item");

        DiscountDto updated = service.update(created.id(),
                new SaveDiscountRequest("  Renamed ", DiscountType.FIXED, new BigDecimal("30"), null, 5L, null, null, false));
        assertThat(updated.name()).isEqualTo("Renamed");
        assertThat(updated.categoryId()).isEqualTo(5L);
        assertThat(updated.menuItemId()).isNull();
        assertThat(updated.active()).isFalse();

        service.delete(created.id());
        assertThat(service.list()).isEmpty();
    }

    @Test
    void shouldRejectBothOrNoTargets() {
        assertThatThrownBy(() -> service.create(new SaveDiscountRequest("X", DiscountType.FIXED, BigDecimal.TEN, 1L, 2L, null, null, true)))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.create(new SaveDiscountRequest("X", DiscountType.FIXED, BigDecimal.TEN, null, null, null, null, true)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldRejectInvalidValues() {
        assertThatThrownBy(() -> service.create(itemPercent("0", 1L))).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.create(itemPercent("100.01", 1L))).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.create(new SaveDiscountRequest("X", DiscountType.FIXED, new BigDecimal("-1"), 1L, null, null, null, true)))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.create(new SaveDiscountRequest(" ", DiscountType.FIXED, BigDecimal.ONE, 1L, null, null, null, true)))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.create(new SaveDiscountRequest("X", null, BigDecimal.ONE, 1L, null, null, null, true)))
                .isInstanceOf(InvalidRequestException.class);
        assertThat(service.create(itemPercent("100", 1L)).value()).isEqualByComparingTo("100");
    }

    @Test
    void shouldRejectInvertedWindow() {
        assertThatThrownBy(() -> service.create(new SaveDiscountRequest("X", DiscountType.FIXED, BigDecimal.ONE, 1L, null,
                NOW, NOW, true))).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldThrowNotFoundForUnknownId() {
        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.update(99L, itemPercent("10", 1L))).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findActiveShouldRespectFlagAndHalfOpenWindow() {
        service.create(new SaveDiscountRequest("starts now", DiscountType.FIXED, BigDecimal.ONE, 1L, null, NOW, null, true));
        service.create(new SaveDiscountRequest("ended now", DiscountType.FIXED, BigDecimal.ONE, 2L, null, null, NOW, true));
        service.create(new SaveDiscountRequest("inactive", DiscountType.FIXED, BigDecimal.ONE, 3L, null, null, null, false));
        service.create(new SaveDiscountRequest("future", DiscountType.FIXED, BigDecimal.ONE, 4L, null, NOW.plusSeconds(1), null, true));

        assertThat(service.findActive(NOW)).extracting(DiscountDto::name).containsExactly("starts now");
    }

    @Test
    void bestDiscountShouldPickLargestBetweenItemAndCategory() {
        service.create(categoryPercent("15", 7L));
        DiscountDto item = service.create(itemPercent("20", 1L));
        service.create(itemPercent("50", 2L)); // other item, must be ignored

        DiscountQuote quote = service.calculateBestDiscount(1L, 7L, new BigDecimal("300"), NOW);

        assertThat(quote.discountId()).isEqualTo(item.id());
        assertThat(quote.discountPerUnit()).isEqualByComparingTo("60.00");
        assertThat(quote.finalUnitPrice()).isEqualByComparingTo("240.00");
    }

    @Test
    void bestDiscountShouldCompareFixedAgainstPercentage() {
        service.create(itemPercent("10", 1L));                                  // 10.00 on 100
        DiscountDto fixed = service.create(new SaveDiscountRequest("Fixed", DiscountType.FIXED,
                new BigDecimal("25"), null, 3L, null, null, true));          // 25.00

        DiscountQuote quote = service.calculateBestDiscount(1L, 3L, new BigDecimal("100"), NOW);

        assertThat(quote.discountId()).isEqualTo(fixed.id());
        assertThat(quote.finalUnitPrice()).isEqualByComparingTo("75.00");
    }

    @Test
    void bestDiscountTieShouldPreferLowerId() {
        DiscountDto first = service.create(itemPercent("10", 1L));
        service.create(categoryPercent("10", 3L));

        assertThat(service.calculateBestDiscount(1L, 3L, new BigDecimal("100"), NOW).discountId()).isEqualTo(first.id());
    }

    @Test
    void fixedDiscountLargerThanPriceShouldGiveZeroNotNegative() {
        service.create(new SaveDiscountRequest("Huge", DiscountType.FIXED, new BigDecimal("500"), 1L, null, null, null, true));

        DiscountQuote quote = service.calculateBestDiscount(1L, null, new BigDecimal("300"), NOW);

        assertThat(quote.discountPerUnit()).isEqualByComparingTo("300.00");
        assertThat(quote.finalUnitPrice()).isEqualByComparingTo("0.00");
    }

    @Test
    void noApplicableDiscountShouldReturnBasePrice() {
        service.create(new SaveDiscountRequest("Inactive", DiscountType.PERCENTAGE, BigDecimal.TEN, 1L, null, null, null, false));

        DiscountQuote quote = service.calculateBestDiscount(1L, null, new BigDecimal("99.9"), NOW);

        assertThat(quote.discountId()).isNull();
        assertThat(quote.baseUnitPrice()).isEqualTo(new BigDecimal("99.90"));
        assertThat(quote.discountPerUnit()).isEqualTo(new BigDecimal("0.00"));
        assertThat(quote.finalUnitPrice()).isEqualTo(new BigDecimal("99.90"));
    }

    @Test
    void percentageShouldRoundHalfUp() {
        service.create(itemPercent("15", 1L)); // 15% of 99.99 = 14.9985 -> 15.00

        DiscountQuote quote = service.calculateBestDiscount(1L, null, new BigDecimal("99.99"), NOW);

        assertThat(quote.discountPerUnit()).isEqualTo(new BigDecimal("15.00"));
        assertThat(quote.finalUnitPrice()).isEqualTo(new BigDecimal("84.99"));
    }

    @Test
    void bestDiscountShouldValidateInput() {
        assertThatThrownBy(() -> service.calculateBestDiscount(null, 1L, BigDecimal.TEN, NOW)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.calculateBestDiscount(1L, 1L, null, NOW)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.calculateBestDiscount(1L, 1L, new BigDecimal("-1"), NOW)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.calculateBestDiscount(1L, 1L, BigDecimal.TEN, null)).isInstanceOf(InvalidRequestException.class);
    }
}
