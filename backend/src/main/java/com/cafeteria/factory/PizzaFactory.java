package com.cafeteria.factory;

import com.cafeteria.model.FoodItem;
import com.cafeteria.model.Pizza;

public class PizzaFactory extends FoodFactory {

    @Override
    public FoodItem createFoodItem(String name, double price) {
        return new Pizza(name, price);
    }
}
