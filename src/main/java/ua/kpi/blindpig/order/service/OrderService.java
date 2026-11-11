package ua.kpi.blindpig.order.service;

import ua.kpi.blindpig.order.dto.*;
import java.util.List;

public interface OrderService {
    /** Checkout: validate cart/address/menu, price again, save order+items, clear cart atomically. */
    FoodOrderDto placeOrder(PlaceOrderRequest request);
    FoodOrderDto getById(Long orderId);
    List<FoodOrderDto> listForCustomer(Long customerId);
    List<FoodOrderDto> listAll();
    /** Payment calls this after PENDING -> PAID. */
    FoodOrderDto confirmPaidOrder(Long orderId);
    /** Delivery calls this after IN_TRANSIT -> DELIVERED. */
    FoodOrderDto completeDeliveredOrder(Long orderId);
    FoodOrderDto cancelOrder(Long orderId);
}
