package com.cafeteria.controller;

import com.cafeteria.entity.FoodItemEntity;
import com.cafeteria.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FoodItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @BeforeEach
    void setUp() {
        if (foodItemRepository.count() == 0) {
            foodItemRepository.save(new FoodItemEntity("Classic Burger", 120.0, "Burger"));
            foodItemRepository.save(new FoodItemEntity("Margherita Pizza", 280.0, "Pizza"));
            foodItemRepository.save(new FoodItemEntity("Cold Drink", 40.0, "Beverage"));
            foodItemRepository.save(new FoodItemEntity("Chocolate Brownie", 90.0, "Dessert"));
        }
    }

    @Test
    @DisplayName("GET /api/food-items returns 200 and list of food items")
    void testGetAllFoodItems() throws Exception {
        mockMvc.perform(get("/api/food-items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].price").exists())
                .andExpect(jsonPath("$[0].category").exists());
    }

    @Test
    @DisplayName("GET /api/food-items?category=Burger filters by category")
    void testGetFoodItemsByCategory() throws Exception {
        mockMvc.perform(get("/api/food-items")
                        .param("category", "Burger")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category", is("Burger")));
    }

    @Test
    @DisplayName("GET /api/food-items?category=pizza filters with case insensitivity")
    void testGetFoodItemsByCategoryCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/food-items")
                        .param("category", "pizza")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category", is("Pizza")));
    }

    @Test
    @DisplayName("CORS allows origin http://localhost:5173 on /api/food-items")
    void testCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/food-items")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
