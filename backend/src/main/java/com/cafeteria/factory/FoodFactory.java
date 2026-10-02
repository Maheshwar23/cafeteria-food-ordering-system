package com.cafeteria.factory;

import com.cafeteria.model.FoodItem;

public abstract class FoodFactory {

    /**
     * Factory Method for creating FoodItem objects.
     * Concrete subclasses decide which concrete FoodItem to instantiate.
     */
    public abstract FoodItem createFoodItem(String name, double price);
}
