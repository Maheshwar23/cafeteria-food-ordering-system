package com.cafeteria.entity;

import com.cafeteria.model.FoodItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing a food item stored in the cafeteria database.
 */
@Entity
@Table(name = "food_items")
public class FoodItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String category;

    public FoodItemEntity() {
    }

    public FoodItemEntity(String name, double price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public FoodItemEntity(FoodItem foodItem) {
        if (foodItem != null) {
            this.name = foodItem.getName();
            this.price = foodItem.getPrice();
            this.category = foodItem.getCategory();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
