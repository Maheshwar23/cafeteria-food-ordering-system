package com.cafeteria.controller.dto;

import com.cafeteria.entity.OrderEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete order returned by GET /api/orders/{id}.
 */
public class OrderResponse {

    private Long id;
    private String address;
    private String specialInstructions;
    private String deliveryOption;
    private String paymentOption;
    private String status;
    private double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items = new ArrayList<>();

    public OrderResponse() {
    }

    public OrderResponse(OrderEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.address = entity.getAddress();
            this.specialInstructions = entity.getSpecialInstructions();
            this.deliveryOption = entity.getDeliveryOption();
            this.paymentOption = entity.getPaymentOption();
            this.status = entity.getStatus() != null ? entity.getStatus().name() : null;
            this.totalAmount = entity.getTotalAmount();
            this.createdAt = entity.getCreatedAt();
            if (entity.getItems() != null) {
                this.items = entity.getItems().stream()
                        .map(OrderItemResponse::new)
                        .toList();
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }
}
