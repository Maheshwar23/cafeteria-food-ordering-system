package com.cafeteria.factory;

import com.cafeteria.model.Dessert;
import com.cafeteria.model.FoodItem;

public class DessertFactory extends FoodFactory {

    @Override
    public FoodItem createFoodItem(String name, double price) {
        return new Dessert(name, price);
    }
}
