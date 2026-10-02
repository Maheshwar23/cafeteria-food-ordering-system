package com.cafeteria.adapter;

/**
 * Target interface expected by the Cafeteria Food Ordering System.
 */
public interface PaymentProcessor {

    /**
     * Processes a payment for the specified monetary amount.
     *
     * @param amount the payment amount
     * @return a PaymentResult indicating success or failure
     */
    PaymentResult processPayment(double amount);
}
