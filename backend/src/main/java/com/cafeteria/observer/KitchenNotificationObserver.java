package com.cafeteria.observer;

import com.cafeteria.model.Order;
import com.cafeteria.model.OrderStatus;

/**
 * Concrete observer that receives status updates and creates notifications for the kitchen/staff.
 */
public class KitchenNotificationObserver implements OrderStatusObserver {

    private String lastNotification;
    private OrderStatus lastStatus;
    private Order lastOrder;

    @Override
    public void update(Order order, OrderStatus newStatus) {
        this.lastOrder = order;
        this.lastStatus = newStatus;
        this.lastNotification = "Kitchen notification: Order status changed to " + newStatus;
    }

    public String getLastNotification() {
        return lastNotification;
    }

    public OrderStatus getLastStatus() {
        return lastStatus;
    }

    public Order getLastOrder() {
        return lastOrder;
    }
}
