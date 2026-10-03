package com.cafeteria.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body for POST /api/orders.
 * discountStrategy format: "NONE" | "PERCENTAGE:10" | "FIXED:50"
 */
public class PlaceOrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    private String address;
    private String specialInstructions;
    private String deliveryOption;
    private String paymentOption;

    /**
     * Pricing/discount strategy to apply.
     * Accepted values: "NONE" (default), "PERCENTAGE:<0-100>", "FIXED:<amount>"
     */
    private String discountStrategy = "NONE";

    public PlaceOrderRequest() {
    }

    public PlaceOrderRequest(List<OrderItemRequest> items, String address, String specialInstructions,
                             String deliveryOption, String paymentOption, String discountStrategy) {
        this.items = items;
        this.address = address;
        this.specialInstructions = specialInstructions;
        this.deliveryOption = deliveryOption;
        this.paymentOption = paymentOption;
        this.discountStrategy = discountStrategy != null ? discountStrategy : "NONE";
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public String getDeliveryOption() {
        return deliveryOption;
    }

    public void setDeliveryOption(String deliveryOption) {
        this.deliveryOption = deliveryOption;
    }

    public String getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(String paymentOption) {
        this.paymentOption = paymentOption;
    }

    public String getDiscountStrategy() {
        return discountStrategy;
    }

    public void setDiscountStrategy(String discountStrategy) {
        this.discountStrategy = discountStrategy;
    }
}
