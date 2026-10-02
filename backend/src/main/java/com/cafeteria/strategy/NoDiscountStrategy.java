package com.cafeteria.strategy;

import com.cafeteria.model.Order;

/**
 * Concrete strategy that returns the regular order total without any discount.
 */
public class NoDiscountStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(Order order) {
        if (order == null) {
            return 0.0;
        }
        return order.calculateTotal();
    }
}
