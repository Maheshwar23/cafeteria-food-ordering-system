package com.cafeteria.observer;

import com.cafeteria.model.Order;
import com.cafeteria.model.OrderStatus;

/**
 * Observer interface for receiving order status updates.
 */
public interface OrderStatusObserver {

    /**
     * Called when an observed order's status changes.
     *
     * @param order the order whose status changed
     * @param newStatus the new status of the order
     */
    void update(Order order, OrderStatus newStatus);
}
