package ua.kpi.blindpig.billing.service;

import ua.kpi.blindpig.billing.dto.*;
import java.util.List;

public interface PaymentService {
    /** One payment per order, amount copied from already-created FoodOrder.total. */
    PaymentDto createForOrder(Long orderId);
    PaymentDto getById(Long paymentId);
    PaymentDto getByOrder(Long orderId);
    List<PaymentDto> listByStatus(PaymentStatus status);
    /** PENDING -> PAID or FAILED exactly once. Success confirms Order in same transaction. */
    PaymentDto simulateResult(Long paymentId, PaymentOutcome outcome);
}
