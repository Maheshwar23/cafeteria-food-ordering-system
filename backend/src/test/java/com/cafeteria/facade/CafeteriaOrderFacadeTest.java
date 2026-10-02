package com.cafeteria.facade;

import com.cafeteria.adapter.MockPayAdapter;
import com.cafeteria.adapter.PaymentProcessor;
import com.cafeteria.adapter.QuickPayAdapter;
import com.cafeteria.adapter.external.MockPayService;
import com.cafeteria.adapter.external.QuickPayService;
import com.cafeteria.factory.BeverageFactory;
import com.cafeteria.factory.BurgerFactory;
import com.cafeteria.factory.FoodFactory;
import com.cafeteria.factory.PizzaFactory;
import com.cafeteria.model.FoodItem;
import com.cafeteria.model.Order;
import com.cafeteria.model.OrderStatus;
import com.cafeteria.observer.CustomerNotificationObserver;
import com.cafeteria.observer.KitchenNotificationObserver;
import com.cafeteria.strategy.FixedAmountDiscountStrategy;
import com.cafeteria.strategy.NoDiscountStrategy;
import com.cafeteria.strategy.PercentageDiscountStrategy;
import com.cafeteria.strategy.PricingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CafeteriaOrderFacadeTest {

    private final FoodFactory burgerFactory = new BurgerFactory();
    private final FoodFactory pizzaFactory = new PizzaFactory();
    private final FoodFactory beverageFactory = new BeverageFactory();

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        // Total = (2 * 100.0) + (1 * 250.0) + (2 * 25.0) = 500.0
        FoodItem burger = burgerFactory.createFoodItem("Veggie Burger", 100.0);
        FoodItem pizza = pizzaFactory.createFoodItem("Margherita Pizza", 250.0);
        FoodItem drink = beverageFactory.createFoodItem("Cold Coffee", 25.0);

        sampleOrder = new Order.Builder()
                .addItem(burger, 2)
                .addItem(pizza, 1)
                .addItem(drink, 2)
                .setAddress("Academic Block 3, Lab 102")
                .setDeliveryOption("Delivery")
                .setPaymentOption("Online")
                .build();
    }

    @Test
    @DisplayName("Successful order placement with NoDiscountStrategy and MockPayAdapter")
    void testSuccessfulOrderPlacementWithMockPay() {
        PaymentProcessor paymentProcessor = new MockPayAdapter(new MockPayService());
        PricingContext pricingContext = new PricingContext(new NoDiscountStrategy());

        CafeteriaOrderFacade facade = new CafeteriaOrderFacade(pricingContext, paymentProcessor);
        OrderPlacementResult result = facade.placeOrder(sampleOrder);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "Order placement should succeed");
        assertEquals(500.0, result.getFinalPrice(), 0.001);
        assertEquals(OrderStatus.PLACED, sampleOrder.getStatus());
        assertNotNull(result.getPaymentResult());
        assertTrue(result.getPaymentResult().isSuccess());
        assertNotNull(result.getPaymentResult().getTransactionId());
        assertSame(sampleOrder, result.getOrder());
    }

    @Test
    @DisplayName("Successful order placement with QuickPayAdapter and PercentageDiscountStrategy")
    void testSuccessfulOrderPlacementWithQuickPayAndDiscount() {
        PaymentProcessor paymentProcessor = new QuickPayAdapter(new QuickPayService());
        // 20% discount on 500 = 400
        PricingContext pricingContext = new PricingContext(new PercentageDiscountStrategy(20.0));

        CafeteriaOrderFacade facade = new CafeteriaOrderFacade(pricingContext, paymentProcessor);
        OrderPlacementResult result = facade.placeOrder(sampleOrder);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "Order placement should succeed");
        assertEquals(400.0, result.getFinalPrice(), 0.001, "Final price should reflect 20% discount");
        assertEquals(OrderStatus.PLACED, sampleOrder.getStatus());
        assertTrue(result.getPaymentResult().getTransactionId().startsWith("QP-REF-"));
    }

    @Test
    @DisplayName("Payment failure when payable amount is zero or negative")
    void testPaymentFailureForZeroAmountOrder() {
        Order emptyOrder = new Order.Builder()
                .setDeliveryOption("Takeaway")
                .build();

        PaymentProcessor paymentProcessor = new MockPayAdapter(new MockPayService());
        PricingContext pricingContext = new PricingContext(new NoDiscountStrategy());

        CafeteriaOrderFacade facade = new CafeteriaOrderFacade(pricingContext, paymentProcessor);
        OrderPlacementResult result = facade.placeOrder(emptyOrder);

        assertNotNull(result);
        assertFalse(result.isSuccess(), "Order placement should fail when payment fails");
        assertEquals(0.0, result.getFinalPrice(), 0.001);
        assertNotNull(result.getPaymentResult());
        assertFalse(result.getPaymentResult().isSuccess());
        assertEquals(OrderStatus.CANCELLED, emptyOrder.getStatus());
    }

    @Test
    @DisplayName("Facade integrates and switches pricing strategies dynamically")
    void testPricingStrategyIntegration() {
        PaymentProcessor paymentProcessor = new QuickPayAdapter(new QuickPayService());
        PricingContext pricingContext = new PricingContext(new FixedAmountDiscountStrategy(100.0));

        CafeteriaOrderFacade facade = new CafeteriaOrderFacade(pricingContext, paymentProcessor);

        // 500 - 100 = 400
        OrderPlacementResult result1 = facade.placeOrder(sampleOrder);
        assertTrue(result1.isSuccess());
        assertEquals(400.0, result1.getFinalPrice(), 0.001);

        // Switch to 50% discount -> 250
        facade.setPricingContext(new PricingContext(new PercentageDiscountStrategy(50.0)));
        OrderPlacementResult result2 = facade.placeOrder(sampleOrder);
        assertTrue(result2.isSuccess());
        assertEquals(250.0, result2.getFinalPrice(), 0.001);
    }

    @Test
    @DisplayName("Facade uses PaymentProcessor polymorphically across different adapters")
    void testPaymentProcessorPolymorphism() {
        PricingContext pricingContext = new PricingContext(new NoDiscountStrategy());

        // Test with MockPayAdapter
        CafeteriaOrderFacade facade1 = new CafeteriaOrderFacade(pricingContext, new MockPayAdapter(new MockPayService()));
        OrderPlacementResult result1 = facade1.placeOrder(sampleOrder);
        assertTrue(result1.isSuccess());
        assertTrue(result1.getPaymentResult().getTransactionId().startsWith("MPAY-INR-"));

        // Test with QuickPayAdapter
        CafeteriaOrderFacade facade2 = new CafeteriaOrderFacade(pricingContext, new QuickPayAdapter(new QuickPayService()));
        OrderPlacementResult result2 = facade2.placeOrder(sampleOrder);
        assertTrue(result2.isSuccess());
        assertTrue(result2.getPaymentResult().getTransactionId().startsWith("QP-REF-"));
    }

    @Test
    @DisplayName("Observer integration: Observers receive PLACED notification when Facade places order")
    void testObserverNotificationThroughFacade() {
        CustomerNotificationObserver customerObserver = new CustomerNotificationObserver();
        KitchenNotificationObserver kitchenObserver = new KitchenNotificationObserver();

        PaymentProcessor paymentProcessor = new MockPayAdapter(new MockPayService());
        CafeteriaOrderFacade facade = new CafeteriaOrderFacade(paymentProcessor);

        OrderPlacementResult result = facade.placeOrder(sampleOrder, customerObserver, kitchenObserver);

        assertTrue(result.isSuccess());
        assertEquals(OrderStatus.PLACED, sampleOrder.getStatus());

        // Check customer observer
        assertEquals(OrderStatus.PLACED, customerObserver.getLastStatus());
        assertEquals("Customer notification: Order status changed to PLACED", customerObserver.getLastNotification());

        // Check kitchen observer
        assertEquals(OrderStatus.PLACED, kitchenObserver.getLastStatus());
        assertEquals("Kitchen notification: Order status changed to PLACED", kitchenObserver.getLastNotification());
    }

    @Test
    @DisplayName("Facade throws IllegalArgumentException on null constructor parameters")
    void testNullValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CafeteriaOrderFacade(null, new MockPayAdapter(new MockPayService())));
        assertThrows(IllegalArgumentException.class, () -> new CafeteriaOrderFacade(new PricingContext(new NoDiscountStrategy()), null));
        assertThrows(IllegalArgumentException.class, () -> new CafeteriaOrderFacade(null));

        CafeteriaOrderFacade facade = new CafeteriaOrderFacade(new MockPayAdapter(new MockPayService()));
        OrderPlacementResult nullOrderResult = facade.placeOrder(null);
        assertFalse(nullOrderResult.isSuccess());
        assertEquals("Order cannot be null", nullOrderResult.getMessage());
    }
}
