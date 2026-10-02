package com.cafeteria.adapter.external;

import java.util.UUID;

/**
 * Mock third-party payment service "MockPay".
 * Incompatible API: exposes makePayment(double amount, String currency).
 * Does NOT implement PaymentProcessor.
 */
public class MockPayService {

    /**
     * Vendor-specific payment method.
     *
     * @param amount the payment amount
     * @param currency the currency code (e.g. INR, USD)
     * @return a transaction token on success, or null on failure
     */
    public String makePayment(double amount, String currency) {
        if (amount <= 0.0) {
            return null;
        }
        return "MPAY-" + (currency != null ? currency : "INR") + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
