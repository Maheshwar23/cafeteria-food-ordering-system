package com.cafeteria.facade;

import com.cafeteria.adapter.PaymentProcessor;
import com.cafeteria.adapter.PaymentResult;
import com.cafeteria.model.Order;
import com.cafeteria.model.OrderStatus;
import com.cafeteria.observer.OrderStatusObserver;
import com.cafeteria.strategy.NoDiscountStrategy;
import com.cafeteria.strategy.PricingContext;

/**
 * Facade providing a unified, simplified entry point for placing cafeteria orders.
 * Coordinates Order (Builder), PricingContext (Strategy), PaymentProcessor (Adapter),
 * and OrderStatusObserver (Observer) without duplicating subsystem responsibilities.
 */
public class CafeteriaOrderFacade {

    private PricingContext pricingContext;
    private PaymentProcessor paymentProcessor;

    public CafeteriaOrderFacade(PricingContext pricingContext, PaymentProcessor paymentProcessor) {
        if (pricingContext == null) {
            throw new IllegalArgumentException("PricingContext cannot be null");
        }
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("PaymentProcessor cannot be null");
        }
        this.pricingContext = pricingContext;
        this.paymentProcessor = paymentProcessor;
    }

    public CafeteriaOrderFacade(PaymentProcessor paymentProcessor) {
        this(new PricingContext(new NoDiscountStrategy()), paymentProcessor);
    }

    public PricingContext getPricingContext() {
        return pricingContext;
    }

    public void setPricingContext(PricingContext pricingContext) {
        if (pricingContext == null) {
            throw new IllegalArgumentException("PricingContext cannot be null");
        }
        this.pricingContext = pricingContext;
    }

    public PaymentProcessor getPaymentProcessor() {
        return paymentProcessor;
    }

    public void setPaymentProcessor(PaymentProcessor paymentProcessor) {
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("PaymentProcessor cannot be null");
        }
        this.paymentProcessor = paymentProcessor;
    }

    /**
     * Coordinates placing an order with optional observers.
     *
     * @param order the order being placed
     * @param observers optional observers to register for status notifications
     * @return OrderPlacementResult containing success flag, order, final price, and payment result
     */
    public OrderPlacementResult placeOrder(Order order, OrderStatusObserver... observers) {
        if (order == null) {
            return OrderPlacementResult.failure("Order cannot be null", null, 0.0, null);
        }

        if (observers != null) {
            for (OrderStatusObserver observer : observers) {
                if (observer != null) {
                    order.addObserver(observer);
                }
            }
        }

        // 1. Calculate final price using Strategy
        double finalPrice = pricingContext.calculatePrice(order);

        // 2. Process payment using Adapter
        PaymentResult paymentResult = paymentProcessor.processPayment(finalPrice);

        // 3. Update status & notify Observers based on payment result
        if (paymentResult != null && paymentResult.isSuccess()) {
            order.setStatus(OrderStatus.PLACED);
            return OrderPlacementResult.success("Order placed successfully", order, finalPrice, paymentResult);
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            String failureMsg = paymentResult != null ? paymentResult.getMessage() : "Payment failed";
            return OrderPlacementResult.failure("Order placement failed: " + failureMsg, order, finalPrice, paymentResult);
        }
    }
}
