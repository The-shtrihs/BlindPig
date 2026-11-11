package ua.kpi.blindpig.order.service;

import ua.kpi.blindpig.order.dto.*;

public interface CartService {
    /** One active cart per customer; creates empty cart if absent. */
    ShoppingCartDto getOrCreateForCustomer(Long customerId);
    ShoppingCartDto addItem(Long customerId, Long menuItemId, int quantity);
    ShoppingCartDto changeQuantity(Long customerId, Long menuItemId, int quantity);
    ShoppingCartDto removeItem(Long customerId, Long menuItemId);
    void clear(Long customerId);
}
