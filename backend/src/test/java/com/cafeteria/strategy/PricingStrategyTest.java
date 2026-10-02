package com.cafeteria.strategy;

import com.cafeteria.factory.BeverageFactory;
import com.cafeteria.factory.BurgerFactory;
import com.cafeteria.factory.FoodFactory;
import com.cafeteria.factory.PizzaFactory;
import com.cafeteria.model.FoodItem;
import com.cafeteria.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingStrategyTest {

    private final FoodFactory burgerFactory = new BurgerFactory();
    private final FoodFactory pizzaFactory = new PizzaFactory();
    private final FoodFactory beverageFactory = new BeverageFactory();

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        // Build an order with known total: (2 * 100.0) + (1 * 250.0) + (2 * 25.0) = 200 + 250 + 50 = 500.0
        FoodItem burger = burgerFactory.createFoodItem("Veggie Burger", 100.0);
        FoodItem pizza = pizzaFactory.createFoodItem("Farmhouse Pizza", 250.0);
        FoodItem drink = beverageFactory.createFoodItem("Cold Coffee", 25.0);

        sampleOrder = new Order.Builder()
                .addItem(burger, 2)
                .addItem(pizza, 1)
                .addItem(drink, 2)
                .setDeliveryOption("Dine-in")
                .setPaymentOption("Cash")
                .build();
    }

    @Test
    @DisplayName("Verify sample order base total calculation")
    void testSampleOrderBaseTotal() {
        assertEquals(500.0, sampleOrder.calculateTotal(), 0.001);
    }

    @Test
    @DisplayName("NoDiscountStrategy returns the normal order total")
    void testNoDiscountStrategy() {
        PricingStrategy strategy = new NoDiscountStrategy();
        PricingContext context = new PricingContext(strategy);

        double price = context.calculatePrice(sampleOrder);

        assertEquals(500.0, price, 0.001, "No discount should return original total");
    }

    @Test
    @DisplayName("PercentageDiscountStrategy correctly applies percentage discount")
    void testPercentageDiscountStrategy() {
        // 10% discount on 500.0 -> 450.0
        PricingStrategy tenPercent = new PercentageDiscountStrategy(10.0);
        PricingContext context = new PricingContext(tenPercent);

        double price = context.calculatePrice(sampleOrder);
        assertEquals(450.0, price, 0.001, "10% discount on 500 should be 450");

        // 25% discount on 500.0 -> 375.0
        PricingStrategy twentyFivePercent = new PercentageDiscountStrategy(25.0);
        context.setStrategy(twentyFivePercent);

        price = context.calculatePrice(sampleOrder);
        assertEquals(375.0, price, 0.001, "25% discount on 500 should be 375");
    }

    @Test
    @DisplayName("FixedAmountDiscountStrategy correctly subtracts fixed discount amount")
    void testFixedAmountDiscountStrategy() {
        // 50.0 discount on 500.0 -> 450.0
        PricingStrategy fiftyOff = new FixedAmountDiscountStrategy(50.0);
        PricingContext context = new PricingContext(fiftyOff);

        double price = context.calculatePrice(sampleOrder);
        assertEquals(450.0, price, 0.001, "50 off 500 should be 450");
    }

    @Test
    @DisplayName("FixedAmountDiscountStrategy never produces a negative final price")
    void testFixedAmountDiscountStrategyFloorAtZero() {
        // 600.0 discount on 500.0 total -> should be 0.0 (not negative)
        PricingStrategy excessiveDiscount = new FixedAmountDiscountStrategy(600.0);
        PricingContext context = new PricingContext(excessiveDiscount);

        double price = context.calculatePrice(sampleOrder);
        assertEquals(0.0, price, 0.001, "Discount exceeding total must floor at zero");
    }

    @Test
    @DisplayName("PricingContext allows dynamic switching of strategies without modifying Order")
    void testDynamicStrategySwitchingOnSameOrder() {
        PricingContext context = new PricingContext(new NoDiscountStrategy());
        assertEquals(500.0, context.calculatePrice(sampleOrder), 0.001);

        // Switch to 15% discount -> 500 - 75 = 425.0
        context.setStrategy(new PercentageDiscountStrategy(15.0));
        assertEquals(425.0, context.calculatePrice(sampleOrder), 0.001);

        // Switch to 80 fixed discount -> 500 - 80 = 420.0
        context.setStrategy(new FixedAmountDiscountStrategy(80.0));
        assertEquals(420.0, context.calculatePrice(sampleOrder), 0.001);

        // Verify base order remained unchanged
        assertEquals(500.0, sampleOrder.calculateTotal(), 0.001);
    }

    @Test
    @DisplayName("Invalid parameters throw IllegalArgumentException")
    void testInvalidStrategyParameters() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscountStrategy(-5.0));
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscountStrategy(105.0));
        assertThrows(IllegalArgumentException.class, () -> new FixedAmountDiscountStrategy(-10.0));
        assertThrows(IllegalArgumentException.class, () -> new PricingContext(null));
    }
}
