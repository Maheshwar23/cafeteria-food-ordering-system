package com.cafeteria.controller;

import com.cafeteria.controller.dto.OrderResponse;
import com.cafeteria.controller.dto.PlaceOrderRequest;
import com.cafeteria.controller.dto.PlaceOrderResponse;
import com.cafeteria.controller.dto.StatusUpdateRequest;
import com.cafeteria.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for order operations.
 *
 * POST  /api/orders              - place a new order
 * GET   /api/orders/{id}         - retrieve an order by ID
 * PATCH /api/orders/{id}/status  - update an order's status
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Place a new order.
     * Orchestrates: Factory Method + Builder + Strategy + Adapter + Facade + Observer + JPA.
     */
    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        PlaceOrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve a single order by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        OrderResponse response = orderService.getOrder(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an order's status.
     * Valid values: PLACED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        Map<String, Object> response = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}
