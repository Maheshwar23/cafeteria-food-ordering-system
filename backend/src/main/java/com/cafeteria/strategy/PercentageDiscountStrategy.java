package com.cafeteria.strategy;

import com.cafeteria.model.Order;

/**
 * Concrete strategy that applies a percentage-based discount to an order total.
 */
public class PercentageDiscountStrategy implements PricingStrategy {

    private final double percentage;

    public PercentageDiscountStrategy(double percentage) {
        if (percentage < 0.0 || percentage > 100.0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        this.percentage = percentage;
    }

    public double getPercentage() {
        return percentage;
    }

    @Override
    public double calculatePrice(Order order) {
        if (order == null) {
            return 0.0;
        }
        double baseTotal = order.calculateTotal();
        double discount = baseTotal * (percentage / 100.0);
        return Math.max(0.0, baseTotal - discount);
    }
}
