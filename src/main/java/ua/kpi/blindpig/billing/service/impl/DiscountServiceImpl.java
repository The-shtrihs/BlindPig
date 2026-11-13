package ua.kpi.blindpig.billing.service.impl;

import org.springframework.stereotype.Service;
import ua.kpi.blindpig.billing.dto.DiscountDto;
import ua.kpi.blindpig.billing.dto.DiscountQuote;
import ua.kpi.blindpig.billing.dto.SaveDiscountRequest;
import ua.kpi.blindpig.billing.model.Discount;
import ua.kpi.blindpig.billing.repository.DiscountRepository;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public DiscountDto create(SaveDiscountRequest request) {
        validate(request);
        Discount discount = new Discount();
        apply(discount, request);
        return toDto(discountRepository.save(discount));
    }

    @Override
    public DiscountDto getById(Long discountId) {
        return toDto(find(discountId));
    }

    @Override
    public List<DiscountDto> list() {
        return discountRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<DiscountDto> findActive(LocalDateTime at) {
        requireAt(at);
        return discountRepository.findActiveAt(at).stream().map(this::toDto).toList();
    }

    @Override
    public DiscountDto update(Long discountId, SaveDiscountRequest request) {
        Discount discount = find(discountId);
        validate(request);
        apply(discount, request);
        return toDto(discountRepository.save(discount));
    }

    @Override
    public void delete(Long discountId) {
        find(discountId);
        discountRepository.deleteById(discountId);
    }

    @Override
    public DiscountQuote calculateBestDiscount(Long menuItemId, Long categoryId,
                                               BigDecimal baseUnitPrice, LocalDateTime at) {
        if (menuItemId == null) {
            throw new InvalidRequestException("Menu item id is required");
        }
        PricingMath.requireNonNegative(baseUnitPrice, "Base unit price");
        requireAt(at);
        BigDecimal base = PricingMath.money(baseUnitPrice);

        record Candidate(Discount discount, BigDecimal amount) {
        }

        return discountRepository.findActiveFor(menuItemId, categoryId, at).stream()
                .map(d -> new Candidate(d, PricingMath.discountAmount(d.getType(), d.getValue(), base)))
                .max(Comparator.comparing(Candidate::amount)
                        .thenComparing(c -> c.discount().getId(), Comparator.reverseOrder()))
                .map(c -> new DiscountQuote(c.discount().getId(), base, c.amount(), base.subtract(c.amount())))
                .orElseGet(() -> new DiscountQuote(null, base, PricingMath.money(BigDecimal.ZERO), base));
    }

    private Discount find(Long discountId) {
        if (discountId == null) {
            throw new InvalidRequestException("Discount id is required");
        }
        return discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount " + discountId + " not found"));
    }

    private static void requireAt(LocalDateTime at) {
        if (at == null) {
            throw new InvalidRequestException("Point in time is required");
        }
    }

    private static void validate(SaveDiscountRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Discount request is required");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidRequestException("Discount name must not be blank");
        }
        PricingMath.validateTypeAndValue(request.type(), request.value());
        if ((request.menuItemId() == null) == (request.categoryId() == null)) {
            throw new InvalidRequestException("Discount must target exactly one of menuItemId or categoryId");
        }
        PricingMath.validateWindow(request.validFrom(), request.validUntil());
    }

    private static void apply(Discount discount, SaveDiscountRequest request) {
        discount.setName(request.name().trim());
        discount.setType(request.type());
        discount.setValue(PricingMath.money(request.value()));
        discount.setMenuItemId(request.menuItemId());
        discount.setCategoryId(request.categoryId());
        discount.setValidFrom(request.validFrom());
        discount.setValidUntil(request.validUntil());
        discount.setActive(request.active());
    }

    private DiscountDto toDto(Discount d) {
        return new DiscountDto(d.getId(), d.getName(), d.getType(), d.getValue(), d.getMenuItemId(),
                d.getCategoryId(), d.getValidFrom(), d.getValidUntil(), d.isActive());
    }
}
