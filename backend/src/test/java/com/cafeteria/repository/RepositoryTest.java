package com.cafeteria.repository;

import com.cafeteria.entity.FoodItemEntity;
import com.cafeteria.entity.OrderEntity;
import com.cafeteria.factory.BeverageFactory;
import com.cafeteria.factory.BurgerFactory;
import com.cafeteria.factory.FoodFactory;
import com.cafeteria.factory.PizzaFactory;
import com.cafeteria.model.FoodItem;
import com.cafeteria.model.Order;
import com.cafeteria.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class RepositoryTest {

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private final FoodFactory burgerFactory = new BurgerFactory();
    private final FoodFactory pizzaFactory = new PizzaFactory();
    private final FoodFactory beverageFactory = new BeverageFactory();

    @Test
    @DisplayName("FoodItemRepository saves and retrieves food items by category")
    void testSaveAndFindFoodItems() {
        FoodItem burger = burgerFactory.createFoodItem("Classic Burger", 120.0);
        FoodItem pizza = pizzaFactory.createFoodItem("Veggie Pizza", 280.0);

        FoodItemEntity savedBurger = foodItemRepository.save(new FoodItemEntity(burger));
        FoodItemEntity savedPizza = foodItemRepository.save(new FoodItemEntity(pizza));

        assertNotNull(savedBurger.getId());
        assertNotNull(savedPizza.getId());

        List<FoodItemEntity> burgers = foodItemRepository.findByCategory("Burger");
        assertEquals(1, burgers.size());
        assertEquals("Classic Burger", burgers.get(0).getName());
        assertEquals(120.0, burgers.get(0).getPrice(), 0.001);

        List<FoodItemEntity> pizzas = foodItemRepository.findByCategory("Pizza");
        assertEquals(1, pizzas.size());
        assertEquals("Veggie Pizza", pizzas.get(0).getName());
    }

    @Test
    @DisplayName("OrderRepository saves order and cascades line items properly")
    void testSaveOrderWithCascadedItems() {
        FoodItem burger = burgerFactory.createFoodItem("Cheese Burger", 150.0);
        FoodItem drink = beverageFactory.createFoodItem("Cold Drink", 40.0);

        Order domainOrder = new Order.Builder()
                .addItem(burger, 2)
                .addItem(drink, 1)
                .setAddress("Library 2nd Floor")
                .setDeliveryOption("Delivery")
                .setPaymentOption("Card")
                .build();

        OrderEntity entity = new OrderEntity(domainOrder);
        OrderEntity savedOrder = orderRepository.save(entity);

        assertNotNull(savedOrder.getId());
        assertEquals(340.0, savedOrder.getTotalAmount(), 0.001);
        assertEquals(2, savedOrder.getItems().size());
        assertEquals(OrderStatus.PLACED, savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedAt());

        Optional<OrderEntity> retrieved = orderRepository.findById(savedOrder.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("Library 2nd Floor", retrieved.get().getAddress());
        assertEquals(2, retrieved.get().getItems().size());
    }

    @Test
    @DisplayName("OrderRepository queries orders by status")
    void testFindOrdersByStatus() {
        FoodItem burger = burgerFactory.createFoodItem("Burger", 100.0);
        Order domainOrder = new Order.Builder().addItem(burger, 1).build();

        OrderEntity order1 = new OrderEntity(domainOrder);
        order1.setStatus(OrderStatus.PLACED);
        orderRepository.save(order1);

        OrderEntity order2 = new OrderEntity(domainOrder);
        order2.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order2);

        List<OrderEntity> placedOrders = orderRepository.findByStatus(OrderStatus.PLACED);
        assertEquals(1, placedOrders.size());

        List<OrderEntity> deliveredOrders = orderRepository.findByStatus(OrderStatus.DELIVERED);
        assertEquals(1, deliveredOrders.size());
    }
}
