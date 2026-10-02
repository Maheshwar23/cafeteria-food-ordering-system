package com.cafeteria.strategy;

import com.cafeteria.model.Order;

/**
 * Strategy interface defining the contract for calculating order pricing.
 */
public interface PricingStrategy {

    /**
     * Calculates the final price for the given order according to this strategy.
     *
     * @param order the order whose price is being calculated
     * @return the final calculated price
     */
    double calculatePrice(Order order);
}
