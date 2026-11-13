package ua.kpi.blindpig.billing.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.kpi.blindpig.billing.dto.DiscountDto;
import ua.kpi.blindpig.billing.dto.DiscountQuote;
import ua.kpi.blindpig.billing.dto.SaveDiscountRequest;
import ua.kpi.blindpig.billing.service.DiscountService;
import ua.kpi.blindpig.billing.web.api.dto.BillingApiError;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Discounts", description = "Автоматичні знижки на страву або категорію")
@RestController
@RequestMapping("/api/discounts")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Некоректні дані",
                content = @Content(schema = @Schema(implementation = BillingApiError.class)))
})
public class DiscountRestController {

    private final DiscountService discountService;
    private final Clock clock;

    public DiscountRestController(DiscountService discountService, Clock clock) {
        this.discountService = discountService;
        this.clock = clock;
    }

    @Operation(summary = "Список знижок", description = "Без параметра — усі; з activeAt — лише чинні на цей момент")
    @ApiResponse(responseCode = "200", description = "Список знижок")
    @GetMapping
    public List<DiscountDto> list(
            @Parameter(description = "ISO date-time, напр. 2026-11-13T12:00:00", example = "2026-11-13T12:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime activeAt) {
        return activeAt == null ? discountService.list() : discountService.findActive(activeAt);
    }

    @Operation(summary = "Знижка за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Знайдено"),
            @ApiResponse(responseCode = "404", description = "Знижку не знайдено",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @GetMapping("/{id}")
    public DiscountDto getById(@PathVariable Long id) {
        return discountService.getById(id);
    }

    @Operation(summary = "Створити знижку", description = "Рівно одне з menuItemId / categoryId; PERCENTAGE у (0,100], FIXED > 0")
    @ApiResponse(responseCode = "201", description = "Створено; Location містить адресу ресурсу")
    @PostMapping
    public ResponseEntity<DiscountDto> create(@Valid @RequestBody SaveDiscountRequest request) {
        DiscountDto created = discountService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Повністю оновити знижку")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Оновлено"),
            @ApiResponse(responseCode = "404", description = "Знижку не знайдено",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @PutMapping("/{id}")
    public DiscountDto update(@PathVariable Long id, @Valid @RequestBody SaveDiscountRequest request) {
        return discountService.update(id, request);
    }

    @Operation(summary = "Видалити знижку")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Видалено"),
            @ApiResponse(responseCode = "404", description = "Знижку не знайдено",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        discountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Найвигідніша автоматична знижка на одиницю",
            description = "Те саме правило, що використовує checkout: одна найбільша знижка (страва або категорія), ціна не нижче 0")
    @ApiResponse(responseCode = "200", description = "Розрахунок ціни одиниці")
    @GetMapping("/best")
    public DiscountQuote best(
            @Parameter(example = "1") @RequestParam Long menuItemId,
            @Parameter(example = "1") @RequestParam(required = false) Long categoryId,
            @Parameter(example = "300.00") @RequestParam BigDecimal basePrice,
            @Parameter(description = "За замовчуванням — зараз") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at) {
        return discountService.calculateBestDiscount(menuItemId, categoryId, basePrice,
                at != null ? at : LocalDateTime.now(clock));
    }
}
