package ua.kpi.blindpig.billing.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.kpi.blindpig.billing.dto.PaymentDto;
import ua.kpi.blindpig.billing.dto.PaymentStatus;
import ua.kpi.blindpig.billing.service.PaymentService;
import ua.kpi.blindpig.billing.web.api.dto.BillingApiError;
import ua.kpi.blindpig.billing.web.api.dto.CreatePaymentRequest;
import ua.kpi.blindpig.billing.web.api.dto.SimulatePaymentRequest;

import java.net.URI;
import java.util.List;

@Tag(name = "Payments", description = "Демонстраційні платежі: PENDING → PAID | FAILED")
@RestController
@RequestMapping("/api/payments")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Некоректні дані",
                content = @Content(schema = @Schema(implementation = BillingApiError.class)))
})
public class PaymentRestController {

    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Список платежів", description = "Фільтр за статусом; без параметра — усі")
    @ApiResponse(responseCode = "200", description = "Список платежів")
    @GetMapping
    public List<PaymentDto> list(@Parameter(example = "PENDING") @RequestParam(required = false) PaymentStatus status) {
        return paymentService.listByStatus(status);
    }

    @Operation(summary = "Платіж за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Знайдено"),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @GetMapping("/{id}")
    public PaymentDto getById(@PathVariable Long id) {
        return paymentService.getById(id);
    }

    @Operation(summary = "Платіж замовлення")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Знайдено"),
            @ApiResponse(responseCode = "404", description = "Для замовлення немає платежу",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @GetMapping("/by-order/{orderId}")
    public PaymentDto getByOrder(@PathVariable Long orderId) {
        return paymentService.getByOrder(orderId);
    }

    @Operation(summary = "Створити платіж для замовлення",
            description = "Сума береться з FoodOrder.total; замовлення має бути CREATED; один платіж на замовлення")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Створено PENDING-платіж"),
            @ApiResponse(responseCode = "404", description = "Замовлення не знайдено",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class))),
            @ApiResponse(responseCode = "409", description = "Платіж уже існує або замовлення не в статусі CREATED",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @PostMapping
    public ResponseEntity<PaymentDto> create(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentDto created = paymentService.createForOrder(request.orderId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Імітувати результат оплати",
            description = "Лише з PENDING і лише один раз. SUCCESS підтверджує замовлення (CREATED → CONFIRMED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Новий стан платежу"),
            @ApiResponse(responseCode = "404", description = "Платіж не знайдено",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class))),
            @ApiResponse(responseCode = "409", description = "Платіж уже PAID/FAILED або замовлення не можна підтвердити",
                    content = @Content(schema = @Schema(implementation = BillingApiError.class)))
    })
    @PostMapping("/{id}/simulate")
    public PaymentDto simulate(@PathVariable Long id, @Valid @RequestBody SimulatePaymentRequest request) {
        return paymentService.simulateResult(id, request.outcome());
    }
}
