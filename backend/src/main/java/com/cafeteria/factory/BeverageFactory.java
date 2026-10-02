package com.cafeteria.factory;

import com.cafeteria.model.Beverage;
import com.cafeteria.model.FoodItem;

public class BeverageFactory extends FoodFactory {

    @Override
    public FoodItem createFoodItem(String name, double price) {
        return new Beverage(name, price);
    }
}
