package com.cafeteria.service;

import com.cafeteria.adapter.MockPayAdapter;
import com.cafeteria.adapter.PaymentProcessor;
import com.cafeteria.adapter.PaymentResult;
import com.cafeteria.adapter.QuickPayAdapter;
import com.cafeteria.adapter.external.MockPayService;
import com.cafeteria.adapter.external.QuickPayService;
import com.cafeteria.controller.dto.OrderItemRequest;
import com.cafeteria.controller.dto.OrderItemResponse;
import com.cafeteria.controller.dto.OrderResponse;
import com.cafeteria.controller.dto.PlaceOrderRequest;
import com.cafeteria.controller.dto.PlaceOrderResponse;
import com.cafeteria.entity.FoodItemEntity;
import com.cafeteria.entity.OrderEntity;
import com.cafeteria.facade.CafeteriaOrderFacade;
import com.cafeteria.facade.OrderPlacementResult;
import com.cafeteria.factory.BeverageFactory;
import com.cafeteria.factory.BurgerFactory;
import com.cafeteria.factory.DessertFactory;
import com.cafeteria.factory.FoodFactory;
import com.cafeteria.factory.PizzaFactory;
import com.cafeteria.model.FoodItem;
import com.cafeteria.model.Order;
import com.cafeteria.model.OrderStatus;
import com.cafeteria.observer.CustomerNotificationObserver;
import com.cafeteria.observer.KitchenNotificationObserver;
import com.cafeteria.repository.FoodItemRepository;
import com.cafeteria.repository.OrderRepository;
import com.cafeteria.strategy.FixedAmountDiscountStrategy;
import com.cafeteria.strategy.NoDiscountStrategy;
import com.cafeteria.strategy.PercentageDiscountStrategy;
import com.cafeteria.strategy.PricingContext;
import com.cafeteria.strategy.PricingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service layer orchestrating the six design patterns for REST order handling.
 *
 * REST request
 *   -> service
 *   -> DB lookup
 *   -> Factory Method
 *   -> Builder
 *   -> Strategy
 *   -> Adapter
 *   -> Facade
 *   -> persistence
 */
@Service
public class OrderService {

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * POST /api/orders
     * Orchestrates: DB lookup -> Factory Method -> Builder -> Strategy -> Adapter -> Facade -> Persistence
     */
    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one item");
        }

        // 1. DB lookup & Factory Method & Builder
        Order.Builder builder = new Order.Builder();

        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
            }
            if (itemReq.getCategory() == null || itemReq.getCategory().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
            }
            if (itemReq.getName() == null || itemReq.getName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Food item name is required");
            }

            // DB lookup - client is NEVER trusted for price
            FoodItemEntity dbItem = foodItemRepository
                    .findByNameIgnoreCaseAndCategoryIgnoreCase(itemReq.getName().trim(), itemReq.getCategory().trim())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Food item not found: \"" + itemReq.getName()
                                    + "\" in category \"" + itemReq.getCategory() + "\""
                    ));

            // Factory Method - instantiate concrete FoodItem subclass
            FoodFactory factory = resolveFactory(itemReq.getCategory());
            FoodItem domainFoodItem = factory.createFoodItem(dbItem.getName(), dbItem.getPrice());

            // Add to Order.Builder
            builder.addItem(domainFoodItem, itemReq.getQuantity());
        }

        // Builder - finalize the domain Order
        Order domainOrder = builder
                .setAddress(request.getAddress())
                .setSpecialInstructions(request.getSpecialInstructions())
                .setDeliveryOption(request.getDeliveryOption())
                .setPaymentOption(request.getPaymentOption())
                .build();

        // 2. Strategy - resolve PricingStrategy
        PricingStrategy pricingStrategy = resolveStrategy(request.getDiscountStrategy());
        PricingContext pricingContext = new PricingContext(pricingStrategy);

        // 3. Adapter - resolve PaymentProcessor
        PaymentProcessor paymentProcessor = resolvePaymentProcessor(request.getPaymentOption());

        // 4. Facade & Observer - coordinate order placement and register observers
        CafeteriaOrderFacade facade = new CafeteriaOrderFacade(pricingContext, paymentProcessor);
        CustomerNotificationObserver customerObserver = new CustomerNotificationObserver();
        KitchenNotificationObserver kitchenObserver = new KitchenNotificationObserver();

        OrderPlacementResult result = facade.placeOrder(domainOrder, customerObserver, kitchenObserver);

        // 5. Check order placement / payment outcome
        if (!result.isSuccess()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, result.getMessage());
        }

        // 6. Persistence - MUST persist result.getFinalPrice(), not raw Order.calculateTotal()
        OrderEntity entity = new OrderEntity(domainOrder);
        entity.setTotalAmount(result.getFinalPrice());
        OrderEntity savedEntity = orderRepository.save(entity);

        String transactionId = (result.getPaymentResult() != null)
                ? result.getPaymentResult().getTransactionId()
                : null;

        List<OrderItemResponse> itemResponses = savedEntity.getItems() != null
                ? savedEntity.getItems().stream().map(OrderItemResponse::new).toList()
                : List.of();

        return new PlaceOrderResponse(
                savedEntity.getId(),
                true,
                result.getMessage(),
                result.getFinalPrice(),
                savedEntity.getStatus().name(),
                transactionId,
                savedEntity.getAddress(),
                savedEntity.getDeliveryOption(),
                savedEntity.getPaymentOption(),
                itemResponses
        );
    }

    /**
     * GET /api/orders/{id}
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id: " + id
                ));
        return new OrderResponse(entity);
    }

    /**
     * PATCH /api/orders/{id}/status
     * Validates status, invokes Observer notification, and persists the update.
     */
    @Transactional
    public Map<String, Object> updateOrderStatus(Long id, String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status cannot be empty");
        }

        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id: " + id
                ));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status: \"" + statusStr
                            + "\". Accepted values: " + Arrays.toString(OrderStatus.values())
            );
        }

        // Invoke existing Observer pattern for domain lifecycle notification
        Order.Builder domainBuilder = new Order.Builder()
                .setAddress(entity.getAddress())
                .setDeliveryOption(entity.getDeliveryOption())
                .setPaymentOption(entity.getPaymentOption())
                .setSpecialInstructions(entity.getSpecialInstructions());

        if (entity.getItems() != null) {
            for (var item : entity.getItems()) {
                try {
                    FoodFactory factory = resolveFactory(item.getFoodCategory());
                    FoodItem domainItem = factory.createFoodItem(item.getFoodName(), item.getUnitPrice());
                    domainBuilder.addItem(domainItem, item.getQuantity());
                } catch (Exception ignored) {
                    // Fallback if category mapping differs
                }
            }
        }
        Order domainOrder = domainBuilder.build();
        domainOrder.setStatus(entity.getStatus());

        CustomerNotificationObserver customerObserver = new CustomerNotificationObserver();
        KitchenNotificationObserver kitchenObserver = new KitchenNotificationObserver();
        domainOrder.addObserver(customerObserver);
        domainOrder.addObserver(kitchenObserver);

        // Notify observers via domain model
        domainOrder.setStatus(newStatus);

        // Persist update in database
        entity.setStatus(newStatus);
        orderRepository.save(entity);

        return Map.of(
                "id", id,
                "status", newStatus.name(),
                "message", "Order status updated successfully"
        );
    }

    /**
     * Resolves concrete FoodFactory by category string.
     */
    private FoodFactory resolveFactory(String category) {
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
        }
        return switch (category.trim().toLowerCase()) {
            case "burger"   -> new BurgerFactory();
            case "pizza"    -> new PizzaFactory();
            case "beverage" -> new BeverageFactory();
            case "dessert"  -> new DessertFactory();
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unknown food category: \"" + category
                            + "\". Valid categories: Burger, Pizza, Beverage, Dessert"
            );
        };
    }

    /**
     * Resolves PricingStrategy from client discountStrategy string.
     * Formats: "NONE", "PERCENTAGE:10", "FIXED:50"
     */
    private PricingStrategy resolveStrategy(String discountStrategy) {
        if (discountStrategy == null || discountStrategy.isBlank()
                || "NONE".equalsIgnoreCase(discountStrategy.trim())) {
            return new NoDiscountStrategy();
        }

        String trimmed = discountStrategy.trim();
        String upper = trimmed.toUpperCase();

        if (upper.startsWith("PERCENTAGE:")) {
            try {
                double pct = Double.parseDouble(trimmed.split(":", 2)[1].trim());
                if (pct < 0.0 || pct > 100.0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount percentage must be between 0 and 100");
                }
                return new PercentageDiscountStrategy(pct);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid percentage discount value");
            }
        }

        if (upper.startsWith("FIXED:")) {
            try {
                double amt = Double.parseDouble(trimmed.split(":", 2)[1].trim());
                if (amt < 0.0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount amount cannot be negative");
                }
                return new FixedAmountDiscountStrategy(amt);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid fixed discount amount");
            }
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown discount strategy: " + discountStrategy);
    }

    /**
     * Resolves PaymentProcessor adapter based on paymentOption string.
     */
    private PaymentProcessor resolvePaymentProcessor(String paymentOption) {
        if (paymentOption == null || paymentOption.isBlank()) {
            return new MockPayAdapter(new MockPayService());
        }

        String option = paymentOption.trim().toLowerCase();
        if (option.contains("mock") || option.contains("card") || option.contains("cash")) {
            return new MockPayAdapter(new MockPayService());
        } else if (option.contains("quick") || option.contains("upi") || option.contains("net banking")) {
            return new QuickPayAdapter(new QuickPayService());
        } else if (option.equals("fail") || option.equals("fail_payment") || option.equals("decline")) {
            return amount -> PaymentResult.failure("Payment rejected by payment gateway");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment option: " + paymentOption);
        }
    }
}
