package com.cafeteria.controller;

import com.cafeteria.entity.FoodItemEntity;
import com.cafeteria.entity.OrderEntity;
import com.cafeteria.model.OrderStatus;
import com.cafeteria.repository.FoodItemRepository;
import com.cafeteria.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private OrderRepository orderRepository;

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
    @DisplayName("POST /api/orders creates order successfully (201 Created) using all design patterns")
    void testCreateOrderSuccess() throws Exception {
        String requestJson = """
                {
                    "items": [
                        { "category": "Burger", "name": "Classic Burger", "quantity": 2 },
                        { "category": "Beverage", "name": "Cold Drink", "quantity": 1 }
                    ],
                    "address": "Room 402, Block A",
                    "specialInstructions": "No onions",
                    "deliveryOption": "Delivery",
                    "paymentOption": "Card",
                    "discountStrategy": "NONE"
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.orderId", notNullValue()))
                .andExpect(jsonPath("$.finalPrice", is(280.0))) // (120*2) + (40*1) = 280
                .andExpect(jsonPath("$.status", is("PLACED")))
                .andExpect(jsonPath("$.transactionId", notNullValue()))
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/orders persists discounted finalPrice when PERCENTAGE discount is applied")
    void testCreateOrderWithPercentageDiscount() throws Exception {
        String requestJson = """
                {
                    "items": [
                        { "category": "Pizza", "name": "Margherita Pizza", "quantity": 1 }
                    ],
                    "address": "Lab 3",
                    "deliveryOption": "Takeaway",
                    "paymentOption": "MockPay",
                    "discountStrategy": "PERCENTAGE:10"
                }
                """;

        String responseStr = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.finalPrice", is(252.0))) // 280 - 10% = 252.0
                .andReturn().getResponse().getContentAsString();

        // Verify in database that total_amount is 252.0 (finalPrice), NOT raw 280.0
        OrderEntity savedOrder = orderRepository.findAll().stream()
                .filter(o -> "Lab 3".equals(o.getAddress()))
                .findFirst()
                .orElseThrow();
        assertEquals(252.0, savedOrder.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("POST /api/orders persists discounted finalPrice when FIXED discount is applied")
    void testCreateOrderWithFixedDiscount() throws Exception {
        String requestJson = """
                {
                    "items": [
                        { "category": "Burger", "name": "Classic Burger", "quantity": 1 }
                    ],
                    "address": "Dean Office",
                    "deliveryOption": "Delivery",
                    "paymentOption": "Card",
                    "discountStrategy": "FIXED:30"
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.finalPrice", is(90.0))); // 120 - 30 = 90.0

        OrderEntity savedOrder = orderRepository.findAll().stream()
                .filter(o -> "Dean Office".equals(o.getAddress()))
                .findFirst()
                .orElseThrow();
        assertEquals(90.0, savedOrder.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("POST /api/orders supports QuickPayAdapter with UPI payment option")
    void testCreateOrderWithQuickPay() throws Exception {
        String requestJson = """
                {
                    "items": [
                        { "category": "Dessert", "name": "Chocolate Brownie", "quantity": 1 }
                    ],
                    "paymentOption": "QuickPay",
                    "discountStrategy": "NONE"
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.transactionId", containsString("QP-REF-")));
    }

    @Test
    @DisplayName("POST /api/orders returns 422 Unprocessable Entity when payment fails")
    void testCreateOrderPaymentFailure() throws Exception {
        String requestJson = """
                {
                    "items": [
                        { "category": "Burger", "name": "Classic Burger", "quantity": 1 }
                    ],
                    "paymentOption": "FAIL"
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/orders returns 404 when food item is not found in database")
    void testCreateOrderFoodItemNotFound() throws Exception {
        String requestJson = """
                {
                    "items": [
                        { "category": "Burger", "name": "Nonexistent Special Burger", "quantity": 1 }
                    ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/orders returns 400 when quantity is zero or negative")
    void testCreateOrderInvalidQuantity() throws Exception {
        String requestJson = """
                {
                    "items": [
                        { "category": "Burger", "name": "Classic Burger", "quantity": 0 }
                    ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/orders returns 400 when items list is empty")
    void testCreateOrderEmptyItems() throws Exception {
        String requestJson = """
                {
                    "items": []
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/orders returns 400 when payment option is unsupported")
    void testCreateOrderUnsupportedPaymentOption() throws Exception {
        String requestJson = """
                {
                    "items": [
                        { "category": "Burger", "name": "Classic Burger", "quantity": 1 }
                    ],
                    "paymentOption": "Cryptocurrency"
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns 200 and complete order details")
    void testGetOrderByIdSuccess() throws Exception {
        // Create an order first
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setAddress("Library 3rd Floor");
        orderEntity.setDeliveryOption("Delivery");
        orderEntity.setPaymentOption("Card");
        orderEntity.setStatus(OrderStatus.PLACED);
        orderEntity.setTotalAmount(150.0);
        OrderEntity saved = orderRepository.save(orderEntity);

        mockMvc.perform(get("/api/orders/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.address", is("Library 3rd Floor")))
                .andExpect(jsonPath("$.status", is("PLACED")))
                .andExpect(jsonPath("$.totalAmount", is(150.0)));
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns 404 when order is not found")
    void testGetOrderByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status updates status and returns 200")
    void testUpdateOrderStatusSuccess() throws Exception {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setStatus(OrderStatus.PLACED);
        orderEntity.setTotalAmount(120.0);
        OrderEntity saved = orderRepository.save(orderEntity);

        String patchJson = """
                {
                    "status": "PREPARING"
                }
                """;

        mockMvc.perform(patch("/api/orders/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PREPARING")));

        // Verify database update
        OrderEntity updated = orderRepository.findById(saved.getId()).orElseThrow();
        assertEquals(OrderStatus.PREPARING, updated.getStatus());
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status returns 400 for invalid status value")
    void testUpdateOrderStatusInvalidStatus() throws Exception {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setStatus(OrderStatus.PLACED);
        orderEntity.setTotalAmount(100.0);
        OrderEntity saved = orderRepository.save(orderEntity);

        String patchJson = """
                {
                    "status": "FLYING_TO_MOON"
                }
                """;

        mockMvc.perform(patch("/api/orders/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status returns 404 for missing order")
    void testUpdateOrderStatusOrderNotFound() throws Exception {
        String patchJson = """
                {
                    "status": "READY"
                }
                """;

        mockMvc.perform(patch("/api/orders/888888/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CORS allows origin http://localhost:5173 on /api/orders")
    void testCorsHeadersOnOrders() throws Exception {
        mockMvc.perform(get("/api/orders/1")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
