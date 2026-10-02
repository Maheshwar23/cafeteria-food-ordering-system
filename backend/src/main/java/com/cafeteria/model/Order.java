package com.cafeteria.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final List<OrderItem> items;
    private final String address;
    private final String specialInstructions;
    private final String deliveryOption;
    private final String paymentOption;

    private Order(Builder builder) {
        this.items = Collections.unmodifiableList(new ArrayList<>(builder.items));
        this.address = builder.address;
        this.specialInstructions = builder.specialInstructions;
        this.deliveryOption = builder.deliveryOption;
        this.paymentOption = builder.paymentOption;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getAddress() {
        return address;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public String getDeliveryOption() {
        return deliveryOption;
    }

    public String getPaymentOption() {
        return paymentOption;
    }

    public double calculateTotal() {
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public static class Builder {
        private final List<OrderItem> items = new ArrayList<>();
        private String address;
        private String specialInstructions;
        private String deliveryOption;
        private String paymentOption;

        public Builder addItem(FoodItem foodItem, int quantity) {
            this.items.add(new OrderItem(foodItem, quantity));
            return this;
        }

        public Builder addItem(FoodItem foodItem) {
            return addItem(foodItem, 1);
        }

        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder setSpecialInstructions(String specialInstructions) {
            this.specialInstructions = specialInstructions;
            return this;
        }

        public Builder setDeliveryOption(String deliveryOption) {
            this.deliveryOption = deliveryOption;
            return this;
        }

        public Builder setPaymentOption(String paymentOption) {
            this.paymentOption = paymentOption;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
