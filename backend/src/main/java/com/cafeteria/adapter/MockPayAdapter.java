package com.cafeteria.adapter;

import com.cafeteria.adapter.external.MockPayService;

/**
 * Adapter that translates the cafeteria's PaymentProcessor.processPayment(double amount)
 * into MockPayService.makePayment(double amount, String currency).
 */
public class MockPayAdapter implements PaymentProcessor {

    private final MockPayService mockPayService;
    private final String currency;

    public MockPayAdapter(MockPayService mockPayService) {
        this(mockPayService, "INR");
    }

    public MockPayAdapter(MockPayService mockPayService, String currency) {
        if (mockPayService == null) {
            throw new IllegalArgumentException("MockPayService cannot be null");
        }
        this.mockPayService = mockPayService;
        this.currency = currency;
    }

    @Override
    public PaymentResult processPayment(double amount) {
        if (amount <= 0.0) {
            return PaymentResult.failure("Payment failed: amount must be greater than zero");
        }

        String confirmationToken = mockPayService.makePayment(amount, currency);
        if (confirmationToken != null && !confirmationToken.isEmpty()) {
            return PaymentResult.success(
                    "MockPay payment of " + currency + " " + amount + " processed successfully",
                    confirmationToken
            );
        } else {
            return PaymentResult.failure("MockPay rejected payment transaction");
        }
    }

    public MockPayService getMockPayService() {
        return mockPayService;
    }
}
