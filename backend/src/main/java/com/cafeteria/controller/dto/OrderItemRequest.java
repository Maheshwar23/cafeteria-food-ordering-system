package com.cafeteria.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a single food item line in a place-order request.
 * The client supplies category, name, and quantity only.
 * Price is always sourced from the database to prevent client price tampering.
 */
public class OrderItemRequest {

    @NotBlank(message = "Food item category is required")
    private String category;

    @NotBlank(message = "Food item name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public OrderItemRequest() {
    }

    public OrderItemRequest(String category, String name, Integer quantity) {
        this.category = category;
        this.name = name;
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
