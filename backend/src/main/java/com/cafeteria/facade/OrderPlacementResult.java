package com.cafeteria.facade;

import com.cafeteria.adapter.PaymentResult;
import com.cafeteria.model.Order;

/**
 * Result object returned by CafeteriaOrderFacade summarizing order placement details.
 */
public class OrderPlacementResult {

    private final boolean success;
    private final String message;
    private final Order order;
    private final double finalPrice;
    private final PaymentResult paymentResult;

    public OrderPlacementResult(boolean success, String message, Order order, double finalPrice, PaymentResult paymentResult) {
        this.success = success;
        this.message = message;
        this.order = order;
        this.finalPrice = finalPrice;
        this.paymentResult = paymentResult;
    }

    public static OrderPlacementResult success(String message, Order order, double finalPrice, PaymentResult paymentResult) {
        return new OrderPlacementResult(true, message, order, finalPrice, paymentResult);
    }

    public static OrderPlacementResult failure(String message, Order order, double finalPrice, PaymentResult paymentResult) {
        return new OrderPlacementResult(false, message, order, finalPrice, paymentResult);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Order getOrder() {
        return order;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public PaymentResult getPaymentResult() {
        return paymentResult;
    }

    @Override
    public String toString() {
        return "OrderPlacementResult [success=" + success + ", message=" + message +
                ", finalPrice=" + finalPrice + ", paymentResult=" + paymentResult + "]";
    }
}
