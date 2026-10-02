package com.cafeteria.model;

import com.cafeteria.factory.BeverageFactory;
import com.cafeteria.factory.BurgerFactory;
import com.cafeteria.factory.DessertFactory;
import com.cafeteria.factory.FoodFactory;
import com.cafeteria.factory.PizzaFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBuilderTest {

    private final FoodFactory burgerFactory = new BurgerFactory();
    private final FoodFactory pizzaFactory = new PizzaFactory();
    private final FoodFactory beverageFactory = new BeverageFactory();
    private final FoodFactory dessertFactory = new DessertFactory();

    @Test
    @DisplayName("Build order with single food item and quantity")
    void testBuildOrderWithSingleItem() {
        FoodItem burger = burgerFactory.createFoodItem("Classic Cheeseburger", 6.50);

        Order order = new Order.Builder()
                .addItem(burger, 2)
                .build();

        assertNotNull(order);
        assertEquals(1, order.getItems().size());
        assertEquals("Classic Cheeseburger", order.getItems().get(0).getFoodItem().getName());
        assertEquals(2, order.getItems().get(0).getQuantity());
        assertEquals(13.00, order.calculateTotal(), 0.001);
    }

    @Test
    @DisplayName("Build order with multiple different food items and different quantities")
    void testBuildOrderWithMultipleItems() {
        FoodItem burger = burgerFactory.createFoodItem("Veggie Deluxe Burger", 7.00);
        FoodItem pizza = pizzaFactory.createFoodItem("Pepperoni Pizza", 12.00);
        FoodItem beverage = beverageFactory.createFoodItem("Iced Tea", 3.00);
        FoodItem dessert = dessertFactory.createFoodItem("Chocolate Cake", 4.50);

        Order order = new Order.Builder()
                .addItem(burger, 2)    // 14.00
                .addItem(pizza, 1)     // 12.00
                .addItem(beverage, 3)  //  9.00
                .addItem(dessert, 2)   //  9.00
                .build();

        assertNotNull(order);
        assertEquals(4, order.getItems().size());
        assertEquals(44.00, order.calculateTotal(), 0.001);
    }

    @Test
    @DisplayName("Build order with all optional details set and retrieved correctly")
    void testBuildOrderWithAllOptionalDetails() {
        FoodItem pizza = pizzaFactory.createFoodItem("Margherita Pizza", 10.00);

        Order order = new Order.Builder()
                .addItem(pizza, 1)
                .setAddress("Campus Block B, Room 304")
                .setSpecialInstructions("Please deliver hot, extra napkins")
                .setDeliveryOption("Delivery")
                .setPaymentOption("Credit Card")
                .build();

        assertNotNull(order);
        assertEquals(1, order.getItems().size());
        assertEquals("Campus Block B, Room 304", order.getAddress());
        assertEquals("Please deliver hot, extra napkins", order.getSpecialInstructions());
        assertEquals("Delivery", order.getDeliveryOption());
        assertEquals("Credit Card", order.getPaymentOption());
        assertEquals(10.00, order.calculateTotal(), 0.001);
    }

    @Test
    @DisplayName("Build order with optional details omitted")
    void testBuildOrderWithOmittedOptionalDetails() {
        FoodItem beverage = beverageFactory.createFoodItem("Lemonade", 2.50);

        Order order = new Order.Builder()
                .addItem(beverage, 1)
                .build();

        assertNotNull(order);
        assertEquals(1, order.getItems().size());
        assertNull(order.getAddress(), "Address should be null when omitted");
        assertNull(order.getSpecialInstructions(), "Special instructions should be null when omitted");
        assertNull(order.getDeliveryOption(), "Delivery option should be null when omitted");
        assertNull(order.getPaymentOption(), "Payment option should be null when omitted");
    }

    @Test
    @DisplayName("Build empty order when no items added yet")
    void testBuildEmptyOrder() {
        Order order = new Order.Builder()
                .setDeliveryOption("Takeaway")
                .build();

        assertNotNull(order);
        assertTrue(order.getItems().isEmpty());
        assertEquals(0.0, order.calculateTotal(), 0.001);
        assertEquals("Takeaway", order.getDeliveryOption());
    }
}
