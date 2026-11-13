package ua.kpi.blindpig.billing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.kpi.blindpig.billing.dto.PaymentDto;
import ua.kpi.blindpig.billing.dto.PaymentOutcome;
import ua.kpi.blindpig.billing.dto.PaymentStatus;
import ua.kpi.blindpig.billing.repository.inmemory.InMemoryPaymentRepository;
import ua.kpi.blindpig.billing.service.PaymentService;
import ua.kpi.blindpig.billing.service.impl.PaymentServiceImpl;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;
import ua.kpi.blindpig.order.dto.FoodOrderDto;
import ua.kpi.blindpig.order.dto.OrderStatus;
import ua.kpi.blindpig.order.service.OrderService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-13T10:00:00Z"), ZoneOffset.UTC);

    private OrderService orderService;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        service = new PaymentServiceImpl(new InMemoryPaymentRepository(), orderService, CLOCK);
    }

    private static FoodOrderDto order(Long id, OrderStatus status, String total) {
        return new FoodOrderDto(id, 7L, "Kyiv", status, List.of(), new BigDecimal("300.00"),
                new BigDecimal("60.00"), new BigDecimal("24.00"), new BigDecimal(total), LocalDateTime.now(CLOCK));
    }

    @Test
    void createShouldCopyOrderTotalAsPendingPayment() {
        when(orderService.getById(1L)).thenReturn(order(1L, OrderStatus.CREATED, "216"));

        PaymentDto payment = service.createForOrder(1L);

        assertThat(payment.id()).isNotNull();
        assertThat(payment.orderId()).isEqualTo(1L);
        assertThat(payment.amount()).isEqualTo(new BigDecimal("216.00"));
        assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.createdAt()).isEqualTo(LocalDateTime.of(2026, 11, 13, 10, 0));
        assertThat(service.getByOrder(1L).id()).isEqualTo(payment.id());
    }

    @Test
    void secondPaymentForSameOrderShouldConflict() {
        when(orderService.getById(1L)).thenReturn(order(1L, OrderStatus.CREATED, "100"));
        service.createForOrder(1L);

        assertThatThrownBy(() -> service.createForOrder(1L)).isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void createShouldRejectOrderThatIsNotCreated() {
        when(orderService.getById(2L)).thenReturn(order(2L, OrderStatus.CANCELLED, "100"));

        assertThatThrownBy(() -> service.createForOrder(2L)).isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void createShouldPropagateUnknownOrder() {
        when(orderService.getById(anyLong())).thenThrow(new ResourceNotFoundException("Order 9 not found"));

        assertThatThrownBy(() -> service.createForOrder(9L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.createForOrder(null)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void successShouldConfirmOrderAndMarkPaid() {
        when(orderService.getById(1L)).thenReturn(order(1L, OrderStatus.CREATED, "216"));
        PaymentDto payment = service.createForOrder(1L);

        PaymentDto paid = service.simulateResult(payment.id(), PaymentOutcome.SUCCESS);

        assertThat(paid.status()).isEqualTo(PaymentStatus.PAID);
        verify(orderService).confirmPaidOrder(1L);
    }

    @Test
    void failureShouldMarkFailedWithoutConfirming() {
        when(orderService.getById(1L)).thenReturn(order(1L, OrderStatus.CREATED, "216"));
        PaymentDto payment = service.createForOrder(1L);

        assertThat(service.simulateResult(payment.id(), PaymentOutcome.FAILURE).status()).isEqualTo(PaymentStatus.FAILED);
        verify(orderService, never()).confirmPaidOrder(anyLong());
    }

    @Test
    void resultCanBeSimulatedOnlyOnce() {
        when(orderService.getById(1L)).thenReturn(order(1L, OrderStatus.CREATED, "216"));
        PaymentDto payment = service.createForOrder(1L);
        service.simulateResult(payment.id(), PaymentOutcome.SUCCESS);

        assertThatThrownBy(() -> service.simulateResult(payment.id(), PaymentOutcome.SUCCESS))
                .isInstanceOf(BusinessConflictException.class);
        assertThatThrownBy(() -> service.simulateResult(payment.id(), PaymentOutcome.FAILURE))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void failedPaymentIsTerminal() {
        when(orderService.getById(1L)).thenReturn(order(1L, OrderStatus.CREATED, "216"));
        PaymentDto payment = service.createForOrder(1L);
        service.simulateResult(payment.id(), PaymentOutcome.FAILURE);

        assertThatThrownBy(() -> service.simulateResult(payment.id(), PaymentOutcome.SUCCESS))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void paymentStaysPendingWhenOrderConfirmationFails() {
        when(orderService.getById(1L)).thenReturn(order(1L, OrderStatus.CREATED, "216"));
        when(orderService.confirmPaidOrder(1L)).thenThrow(new BusinessConflictException("Order 1 was cancelled"));
        PaymentDto payment = service.createForOrder(1L);

        assertThatThrownBy(() -> service.simulateResult(payment.id(), PaymentOutcome.SUCCESS))
                .isInstanceOf(BusinessConflictException.class);
        assertThat(service.getById(payment.id()).status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void listByStatusShouldFilterAndNullMeansAll() {
        when(orderService.getById(1L)).thenReturn(order(1L, OrderStatus.CREATED, "10"));
        when(orderService.getById(2L)).thenReturn(order(2L, OrderStatus.CREATED, "20"));
        PaymentDto first = service.createForOrder(1L);
        service.createForOrder(2L);
        service.simulateResult(first.id(), PaymentOutcome.FAILURE);

        assertThat(service.listByStatus(PaymentStatus.PENDING)).extracting(PaymentDto::orderId).containsExactly(2L);
        assertThat(service.listByStatus(PaymentStatus.FAILED)).extracting(PaymentDto::orderId).containsExactly(1L);
        assertThat(service.listByStatus(PaymentStatus.PAID)).isEmpty();
        assertThat(service.listByStatus(null)).hasSize(2);
    }

    @Test
    void shouldValidateIdsAndOutcome() {
        assertThatThrownBy(() -> service.getById(5L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.getByOrder(5L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.simulateResult(5L, PaymentOutcome.SUCCESS)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.simulateResult(5L, null)).isInstanceOf(InvalidRequestException.class);
    }
}
