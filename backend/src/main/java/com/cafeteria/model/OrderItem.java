package com.cafeteria.model;

public class OrderItem {
    private final FoodItem foodItem;
    private final int quantity;

    public OrderItem(FoodItem foodItem, int quantity) {
        if (foodItem == null) {
            throw new IllegalArgumentException("Food item cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.foodItem = foodItem;
        this.quantity = quantity;
    }

    public FoodItem getFoodItem() {
        return foodItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return foodItem.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return foodItem.getName() + " x " + quantity + " ($" + String.format("%.2f", getTotalPrice()) + ")";
    }
}
