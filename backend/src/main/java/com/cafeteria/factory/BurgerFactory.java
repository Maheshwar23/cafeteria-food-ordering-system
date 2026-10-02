package com.cafeteria.factory;

import com.cafeteria.model.Burger;
import com.cafeteria.model.FoodItem;

public class BurgerFactory extends FoodFactory {

    @Override
    public FoodItem createFoodItem(String name, double price) {
        return new Burger(name, price);
    }
}
