package com.cafeteria.strategy;

import com.cafeteria.model.Order;

/**
 * Context class in the Strategy pattern.
 * Holds a reference to a PricingStrategy and delegates price calculation to it.
 */
public class PricingContext {

    private PricingStrategy strategy;

    public PricingContext(PricingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("PricingStrategy cannot be null");
        }
        this.strategy = strategy;
    }

    public void setStrategy(PricingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("PricingStrategy cannot be null");
        }
        this.strategy = strategy;
    }

    public PricingStrategy getStrategy() {
        return strategy;
    }

    public double calculatePrice(Order order) {
        return strategy.calculatePrice(order);
    }
}
