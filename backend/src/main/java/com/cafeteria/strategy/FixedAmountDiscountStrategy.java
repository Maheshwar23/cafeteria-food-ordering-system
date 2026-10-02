package com.cafeteria.strategy;

import com.cafeteria.model.Order;

/**
 * Concrete strategy that subtracts a fixed discount amount from the order total.
 * Guarantees the final calculated price will never be negative.
 */
public class FixedAmountDiscountStrategy implements PricingStrategy {

    private final double discountAmount;

    public FixedAmountDiscountStrategy(double discountAmount) {
        if (discountAmount < 0.0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        this.discountAmount = discountAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    @Override
    public double calculatePrice(Order order) {
        if (order == null) {
            return 0.0;
        }
        double baseTotal = order.calculateTotal();
        return Math.max(0.0, baseTotal - discountAmount);
    }
}
