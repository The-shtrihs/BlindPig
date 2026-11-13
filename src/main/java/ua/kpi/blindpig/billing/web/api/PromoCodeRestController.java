package ua.kpi.blindpig.billing.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.kpi.blindpig.billing.dto.PromoCodeDto;
import ua.kpi.blindpig.billing.dto.PromoQuote;
import ua.kpi.blindpig.billing.dto.SavePromoCodeRequest;
import ua.kpi.blindpig.billing.service.PromoCodeService;
import ua.kpi.blindpig.billing.web.api.dto.ApplyPromoRequest;
import ua.kpi.blindpig.billing.web.api.dto.BillingApiError;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Promo codes", description = "Промокоди на все замовлення (після автоматичних знижок)")
@RestController
@RequestMapping("/api/promo-codes")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Некоректні дані",
                content = @Content(schema = @Schema(implementation = BillingApiError.class)))
})
public class PromoCodeRestController {

    private final PromoCodeService promoCodeService;
    private final Clock clock;

    public PromoCodeRestController(PromoCodeService promoCodeService, Clock clock) {
        this.promoCodeService = promoCodeService;
        this.clock = clock;
    }

    @Operation(summary = "Список промокодів")
    @ApiResponse(responseCode = "200", description = "Список промокодів")
    @GetMapping
    public List<PromoCodeDto> list() {
        return promoCodeService.list();
    }

    @Operation(summary = "Промокод за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Знайдено"),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @GetMapping("/{id}")
    public PromoCodeDto getById(@PathVariable Long id) {
        return promoCodeService.getById(id);
    }

    @Operation(summary = "Промокод за кодом (без урахування регістру)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Знайдено"),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @GetMapping("/code/{code}")
    public PromoCodeDto getByCode(@PathVariable String code) {
        return promoCodeService.getByCode(code);
    }

    @Operation(summary = "Створити промокод")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Створено; Location містить адресу ресурсу"),
            @ApiResponse(responseCode = "409", description = "Такий код уже існує",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @PostMapping
    public ResponseEntity<PromoCodeDto> create(@Valid @RequestBody SavePromoCodeRequest request) {
        PromoCodeDto created = promoCodeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Повністю оновити промокод")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Оновлено"),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class))),
            @ApiResponse(responseCode = "409", description = "Код зайнятий іншим промокодом",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @PutMapping("/{id}")
    public PromoCodeDto update(@PathVariable Long id, @Valid @RequestBody SavePromoCodeRequest request) {
        return promoCodeService.update(id, request);
    }

    @Operation(summary = "Видалити промокод")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Видалено"),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promoCodeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Перевірити й застосувати промокод до суми",
            description = "Сума — після автоматичних знижок. Невідомий/неактивний/прострочений код або сума нижче мінімальної → 409")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Розмір знижки за промокодом"),
            @ApiResponse(responseCode = "409", description = "Промокод не можна застосувати",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @PostMapping("/apply")
    public PromoQuote apply(@Valid @RequestBody ApplyPromoRequest request) {
        LocalDateTime at = request.at() != null ? request.at() : LocalDateTime.now(clock);
        return promoCodeService.applyPromo(request.code(), request.subtotal(), at);
    }
}
