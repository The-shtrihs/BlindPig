package ua.kpi.blindpig.billing.service.impl;

import org.springframework.stereotype.Service;
import ua.kpi.blindpig.billing.dto.PromoCodeDto;
import ua.kpi.blindpig.billing.dto.PromoQuote;
import ua.kpi.blindpig.billing.dto.SavePromoCodeRequest;
import ua.kpi.blindpig.billing.model.PromoCode;
import ua.kpi.blindpig.billing.repository.PromoCodeRepository;
import ua.kpi.blindpig.billing.service.PromoCodeService;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class PromoCodeServiceImpl implements PromoCodeService {

    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z0-9_-]{3,32}");

    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeServiceImpl(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    @Override
    public PromoCodeDto create(SavePromoCodeRequest request) {
        String code = validate(request);
        if (promoCodeRepository.existsByCode(code)) {
            throw new BusinessConflictException("Promo code " + code + " already exists");
        }
        PromoCode promoCode = new PromoCode();
        apply(promoCode, code, request);
        return toDto(promoCodeRepository.save(promoCode));
    }

    @Override
    public PromoCodeDto getById(Long promoCodeId) {
        return toDto(find(promoCodeId));
    }

    @Override
    public PromoCodeDto getByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidRequestException("Promo code must not be blank");
        }
        return promoCodeRepository.findByCode(code)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code " + code.trim() + " not found"));
    }

    @Override
    public List<PromoCodeDto> list() {
        return promoCodeRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public PromoCodeDto update(Long promoCodeId, SavePromoCodeRequest request) {
        PromoCode promoCode = find(promoCodeId);
        String code = validate(request);
        promoCodeRepository.findByCode(code)
                .filter(other -> !other.getId().equals(promoCodeId))
                .ifPresent(other -> {
                    throw new BusinessConflictException("Promo code " + code + " already exists");
                });
        apply(promoCode, code, request);
        return toDto(promoCodeRepository.save(promoCode));
    }

    @Override
    public void delete(Long promoCodeId) {
        find(promoCodeId);
        promoCodeRepository.deleteById(promoCodeId);
    }

    @Override
    public PromoQuote applyPromo(String code, BigDecimal subtotalAfterAuto, LocalDateTime at) {
        if (code == null || code.isBlank()) {
            throw new InvalidRequestException("Promo code must not be blank");
        }
        PricingMath.requireNonNegative(subtotalAfterAuto, "Subtotal");
        if (at == null) {
            throw new InvalidRequestException("Point in time is required");
        }
        String normalized = normalize(code);
        PromoCode promo = promoCodeRepository.findByCode(normalized)
                .orElseThrow(() -> new BusinessConflictException("Promo code " + normalized + " is not valid"));
        if (!PricingMath.isActiveAt(promo.isActive(), promo.getValidFrom(), promo.getValidUntil(), at)) {
            throw new BusinessConflictException("Promo code " + promo.getCode() + " is not active");
        }
        BigDecimal subtotal = PricingMath.money(subtotalAfterAuto);
        if (promo.getMinOrderAmount() != null && subtotal.compareTo(promo.getMinOrderAmount()) < 0) {
            throw new BusinessConflictException("Promo code " + promo.getCode()
                    + " requires a minimum order of " + promo.getMinOrderAmount());
        }
        BigDecimal amount = PricingMath.discountAmount(promo.getType(), promo.getValue(), subtotal);
        return new PromoQuote(promo.getId(), promo.getCode(), amount);
    }

    private PromoCode find(Long promoCodeId) {
        if (promoCodeId == null) {
            throw new InvalidRequestException("Promo code id is required");
        }
        return promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code " + promoCodeId + " not found"));
    }

    /** Validates the request and returns the normalized (trimmed, upper-case) code. */
    private static String validate(SavePromoCodeRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Promo code request is required");
        }
        if (request.code() == null || request.code().isBlank()) {
            throw new InvalidRequestException("Promo code must not be blank");
        }
        String code = normalize(request.code());
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new InvalidRequestException("Promo code must be 3-32 characters: letters, digits, '-' or '_'");
        }
        PricingMath.validateTypeAndValue(request.type(), request.value());
        if (request.minOrderAmount() != null && request.minOrderAmount().signum() < 0) {
            throw new InvalidRequestException("Minimum order amount must not be negative");
        }
        PricingMath.validateWindow(request.validFrom(), request.validUntil());
        return code;
    }

    private static String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static void apply(PromoCode promoCode, String code, SavePromoCodeRequest request) {
        promoCode.setCode(code);
        promoCode.setType(request.type());
        promoCode.setValue(PricingMath.money(request.value()));
        promoCode.setMinOrderAmount(request.minOrderAmount() == null ? null : PricingMath.money(request.minOrderAmount()));
        promoCode.setValidFrom(request.validFrom());
        promoCode.setValidUntil(request.validUntil());
        promoCode.setActive(request.active());
    }

    private PromoCodeDto toDto(PromoCode p) {
        return new PromoCodeDto(p.getId(), p.getCode(), p.getType(), p.getValue(), p.getMinOrderAmount(),
                p.getValidFrom(), p.getValidUntil(), p.isActive());
    }
}
