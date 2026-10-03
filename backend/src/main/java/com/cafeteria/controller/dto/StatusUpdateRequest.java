package com.cafeteria.controller.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for PATCH /api/orders/{id}/status.
 * Valid status values: PLACED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
 */
public class StatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
