package com.cafeteria.factory;

import com.cafeteria.model.Beverage;
import com.cafeteria.model.Burger;
import com.cafeteria.model.Dessert;
import com.cafeteria.model.FoodItem;
import com.cafeteria.model.Pizza;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FoodFactoryTest {

    @Test
    @DisplayName("BurgerFactory creates a Burger food item with correct properties")
    void testBurgerFactoryCreatesBurger() {
        FoodFactory factory = new BurgerFactory();
        FoodItem item = factory.createFoodItem("Veggie Burger", 5.99);

        assertNotNull(item, "Created item should not be null");
        assertInstanceOf(Burger.class, item, "Created item should be an instance of Burger");
        assertInstanceOf(FoodItem.class, item, "Created item should be an instance of FoodItem");
        assertEquals("Veggie Burger", item.getName(), "Name should match");
        assertEquals(5.99, item.getPrice(), 0.001, "Price should match");
        assertEquals("Burger", item.getCategory(), "Category should be Burger");
    }

    @Test
    @DisplayName("PizzaFactory creates a Pizza food item with correct properties")
    void testPizzaFactoryCreatesPizza() {
        FoodFactory factory = new PizzaFactory();
        FoodItem item = factory.createFoodItem("Margherita Pizza", 8.49);

        assertNotNull(item, "Created item should not be null");
        assertInstanceOf(Pizza.class, item, "Created item should be an instance of Pizza");
        assertInstanceOf(FoodItem.class, item, "Created item should be an instance of FoodItem");
        assertEquals("Margherita Pizza", item.getName(), "Name should match");
        assertEquals(8.49, item.getPrice(), 0.001, "Price should match");
        assertEquals("Pizza", item.getCategory(), "Category should be Pizza");
    }

    @Test
    @DisplayName("BeverageFactory creates a Beverage food item with correct properties")
    void testBeverageFactoryCreatesBeverage() {
        FoodFactory factory = new BeverageFactory();
        FoodItem item = factory.createFoodItem("Iced Latte", 3.25);

        assertNotNull(item, "Created item should not be null");
        assertInstanceOf(Beverage.class, item, "Created item should be an instance of Beverage");
        assertInstanceOf(FoodItem.class, item, "Created item should be an instance of FoodItem");
        assertEquals("Iced Latte", item.getName(), "Name should match");
        assertEquals(3.25, item.getPrice(), 0.001, "Price should match");
        assertEquals("Beverage", item.getCategory(), "Category should be Beverage");
    }

    @Test
    @DisplayName("DessertFactory creates a Dessert food item with correct properties")
    void testDessertFactoryCreatesDessert() {
        FoodFactory factory = new DessertFactory();
        FoodItem item = factory.createFoodItem("Chocolate Brownie", 4.50);

        assertNotNull(item, "Created item should not be null");
        assertInstanceOf(Dessert.class, item, "Created item should be an instance of Dessert");
        assertInstanceOf(FoodItem.class, item, "Created item should be an instance of FoodItem");
        assertEquals("Chocolate Brownie", item.getName(), "Name should match");
        assertEquals(4.50, item.getPrice(), 0.001, "Price should match");
        assertEquals("Dessert", item.getCategory(), "Category should be Dessert");
    }
}
