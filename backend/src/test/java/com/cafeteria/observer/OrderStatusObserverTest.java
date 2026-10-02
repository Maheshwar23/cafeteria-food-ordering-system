package com.cafeteria.observer;

import com.cafeteria.factory.BurgerFactory;
import com.cafeteria.factory.FoodFactory;
import com.cafeteria.model.FoodItem;
import com.cafeteria.model.Order;
import com.cafeteria.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderStatusObserverTest {

    private final FoodFactory burgerFactory = new BurgerFactory();
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        FoodItem burger = burgerFactory.createFoodItem("Cheeseburger", 5.99);
        sampleOrder = new Order.Builder()
                .addItem(burger, 1)
                .setAddress("Hostel Block C, Room 201")
                .setDeliveryOption("Delivery")
                .build();
    }

    @Test
    @DisplayName("Newly built order has initial status PLACED")
    void testInitialOrderStatusIsPlaced() {
        assertNotNull(sampleOrder.getStatus());
        assertEquals(OrderStatus.PLACED, sampleOrder.getStatus(), "Initial status must be PLACED");
    }

    @Test
    @DisplayName("Single registered observer receives notification when status changes")
    void testSingleObserverNotifiedOnStatusChange() {
        CustomerNotificationObserver customerObserver = new CustomerNotificationObserver();
        sampleOrder.addObserver(customerObserver);

        sampleOrder.setStatus(OrderStatus.PREPARING);

        assertEquals(OrderStatus.PREPARING, sampleOrder.getStatus());
        assertEquals(OrderStatus.PREPARING, customerObserver.getLastStatus());
        assertSame(sampleOrder, customerObserver.getLastOrder());
        assertEquals("Customer notification: Order status changed to PREPARING", customerObserver.getLastNotification());
    }

    @Test
    @DisplayName("Multiple observers receive notifications simultaneously when status changes")
    void testMultipleObserversNotified() {
        CustomerNotificationObserver customerObserver = new CustomerNotificationObserver();
        KitchenNotificationObserver kitchenObserver = new KitchenNotificationObserver();

        sampleOrder.addObserver(customerObserver);
        sampleOrder.addObserver(kitchenObserver);

        sampleOrder.setStatus(OrderStatus.READY);

        // Verify customer observer
        assertEquals(OrderStatus.READY, customerObserver.getLastStatus());
        assertEquals("Customer notification: Order status changed to READY", customerObserver.getLastNotification());

        // Verify kitchen observer
        assertEquals(OrderStatus.READY, kitchenObserver.getLastStatus());
        assertEquals("Kitchen notification: Order status changed to READY", kitchenObserver.getLastNotification());
    }

    @Test
    @DisplayName("Removed observer no longer receives subsequent status notifications")
    void testRemovedObserverDoesNotReceiveNotifications() {
        CustomerNotificationObserver customerObserver = new CustomerNotificationObserver();
        KitchenNotificationObserver kitchenObserver = new KitchenNotificationObserver();

        sampleOrder.addObserver(customerObserver);
        sampleOrder.addObserver(kitchenObserver);

        // Status change 1 -> both receive
        sampleOrder.setStatus(OrderStatus.PREPARING);
        assertEquals(OrderStatus.PREPARING, customerObserver.getLastStatus());
        assertEquals(OrderStatus.PREPARING, kitchenObserver.getLastStatus());

        // Remove kitchen observer
        sampleOrder.removeObserver(kitchenObserver);

        // Status change 2 -> only customer receives
        sampleOrder.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, customerObserver.getLastStatus());
        assertEquals(OrderStatus.PREPARING, kitchenObserver.getLastStatus(), "Kitchen observer should remain at PREPARING");
    }

    @Test
    @DisplayName("Lifecycle status transitions trigger notifications at each step")
    void testLifecycleTransitions() {
        CustomerNotificationObserver customerObserver = new CustomerNotificationObserver();
        sampleOrder.addObserver(customerObserver);

        OrderStatus[] lifecycle = {
                OrderStatus.PREPARING,
                OrderStatus.READY,
                OrderStatus.OUT_FOR_DELIVERY,
                OrderStatus.DELIVERED
        };

        for (OrderStatus status : lifecycle) {
            sampleOrder.setStatus(status);
            assertEquals(status, sampleOrder.getStatus());
            assertEquals(status, customerObserver.getLastStatus());
        }
    }

    @Test
    @DisplayName("Order cancellation triggers notification")
    void testOrderCancellationNotification() {
        CustomerNotificationObserver customerObserver = new CustomerNotificationObserver();
        sampleOrder.addObserver(customerObserver);

        sampleOrder.setStatus(OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, sampleOrder.getStatus());
        assertEquals(OrderStatus.CANCELLED, customerObserver.getLastStatus());
    }

    @Test
    @DisplayName("Setting null status throws IllegalArgumentException")
    void testNullStatusThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> sampleOrder.setStatus(null));
    }
}
