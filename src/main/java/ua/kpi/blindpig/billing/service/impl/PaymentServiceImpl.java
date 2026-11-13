package ua.kpi.blindpig.billing.service.impl;

import org.springframework.stereotype.Service;
import ua.kpi.blindpig.billing.dto.PaymentDto;
import ua.kpi.blindpig.billing.dto.PaymentOutcome;
import ua.kpi.blindpig.billing.dto.PaymentStatus;
import ua.kpi.blindpig.billing.model.Payment;
import ua.kpi.blindpig.billing.repository.PaymentRepository;
import ua.kpi.blindpig.billing.service.PaymentService;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;
import ua.kpi.blindpig.order.dto.FoodOrderDto;
import ua.kpi.blindpig.order.dto.OrderStatus;
import ua.kpi.blindpig.order.service.OrderService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final Clock clock;

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderService orderService, Clock clock) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.clock = clock;
    }

    @Override
    public PaymentDto createForOrder(Long orderId) {
        if (orderId == null) {
            throw new InvalidRequestException("Order id is required");
        }
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new BusinessConflictException("Payment for order " + orderId + " already exists");
        }
        FoodOrderDto order = orderService.getById(orderId);
        if (order.status() != OrderStatus.CREATED) {
            throw new BusinessConflictException("Order " + orderId + " is " + order.status()
                    + "; only CREATED orders can be paid");
        }
        Payment payment = new Payment(null, orderId, PricingMath.money(order.total()),
                PaymentStatus.PENDING, LocalDateTime.now(clock));
        return toDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentDto getById(Long paymentId) {
        return toDto(find(paymentId));
    }

    @Override
    public PaymentDto getByOrder(Long orderId) {
        if (orderId == null) {
            throw new InvalidRequestException("Order id is required");
        }
        return paymentRepository.findByOrderId(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order " + orderId + " not found"));
    }

    /** {@code status == null} returns all payments. */
    @Override
    public List<PaymentDto> listByStatus(PaymentStatus status) {
        List<Payment> payments = status == null ? paymentRepository.findAll() : paymentRepository.findByStatus(status);
        return payments.stream().map(this::toDto).toList();
    }

    @Override
    public PaymentDto simulateResult(Long paymentId, PaymentOutcome outcome) {
        if (outcome == null) {
            throw new InvalidRequestException("Payment outcome is required");
        }
        Payment payment = find(paymentId);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessConflictException("Payment " + paymentId + " is already " + payment.getStatus());
        }
        if (outcome == PaymentOutcome.SUCCESS) {
            // Confirm first: if the order refuses, the payment must stay PENDING.
            orderService.confirmPaidOrder(payment.getOrderId());
            payment.setStatus(PaymentStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }
        return toDto(paymentRepository.save(payment));
    }

    private Payment find(Long paymentId) {
        if (paymentId == null) {
            throw new InvalidRequestException("Payment id is required");
        }
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment " + paymentId + " not found"));
    }

    private PaymentDto toDto(Payment p) {
        return new PaymentDto(p.getId(), p.getOrderId(), p.getAmount(), p.getStatus(), p.getCreatedAt());
    }
}
