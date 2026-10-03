package com.cafeteria.controller.dto;

import java.util.List;

/**
 * Response body for POST /api/orders.
 */
public class PlaceOrderResponse {

    private Long orderId;
    private boolean success;
    private String message;
    private double finalPrice;
    private String status;
    private String transactionId;
    private String address;
    private String deliveryOption;
    private String paymentOption;
    private List<OrderItemResponse> items;

    public PlaceOrderResponse() {
    }

    public PlaceOrderResponse(Long orderId, boolean success, String message,
                              double finalPrice, String status, String transactionId) {
        this(orderId, success, message, finalPrice, status, transactionId, null, null, null, null);
    }

    public PlaceOrderResponse(Long orderId, boolean success, String message,
                              double finalPrice, String status, String transactionId,
                              String address, String deliveryOption, String paymentOption,
                              List<OrderItemResponse> items) {
        this.orderId = orderId;
        this.success = success;
        this.message = message;
        this.finalPrice = finalPrice;
        this.status = status;
        this.transactionId = transactionId;
        this.address = address;
        this.deliveryOption = deliveryOption;
        this.paymentOption = paymentOption;
        this.items = items;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeliveryOption() {
        return deliveryOption;
    }

    public void setDeliveryOption(String deliveryOption) {
        this.deliveryOption = deliveryOption;
    }

    public String getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(String paymentOption) {
        this.paymentOption = paymentOption;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }
}
