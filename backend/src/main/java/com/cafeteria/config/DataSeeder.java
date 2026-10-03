package com.cafeteria.config;

import com.cafeteria.entity.FoodItemEntity;
import com.cafeteria.repository.FoodItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the food_items table with sample data on first startup (when the table is empty).
 * Ensures GET /api/food-items always returns a useful result for demonstration.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (foodItemRepository.count() > 0) {
            System.out.println("[DataSeeder] Food items already present - skipping seed.");
            return;
        }

        List<FoodItemEntity> seedItems = List.of(
                new FoodItemEntity("Classic Burger",    120.0, "Burger"),
                new FoodItemEntity("Cheese Burger",     150.0, "Burger"),
                new FoodItemEntity("Margherita Pizza",  280.0, "Pizza"),
                new FoodItemEntity("Veggie Pizza",      260.0, "Pizza"),
                new FoodItemEntity("Cold Drink",         40.0, "Beverage"),
                new FoodItemEntity("Mango Lassi",        60.0, "Beverage"),
                new FoodItemEntity("Chocolate Brownie",  90.0, "Dessert"),
                new FoodItemEntity("Ice Cream",          70.0, "Dessert")
        );

        foodItemRepository.saveAll(seedItems);
        System.out.println("[DataSeeder] Seeded " + seedItems.size() + " food items.");
    }
}
