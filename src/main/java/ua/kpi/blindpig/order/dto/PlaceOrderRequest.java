package ua.kpi.blindpig.order.dto;

public record PlaceOrderRequest(Long customerId, Long addressId, String promoCode) {}
