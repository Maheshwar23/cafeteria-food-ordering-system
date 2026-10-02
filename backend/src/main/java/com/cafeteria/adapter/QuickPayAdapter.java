package com.cafeteria.adapter;

import com.cafeteria.adapter.external.QuickPayService;
import com.cafeteria.adapter.external.QuickPayService.QuickPayResponse;

/**
 * Adapter that translates the cafeteria's PaymentProcessor.processPayment(double amount)
 * into QuickPayService.pay(double amountInRupees).
 */
public class QuickPayAdapter implements PaymentProcessor {

    private final QuickPayService quickPayService;

    public QuickPayAdapter(QuickPayService quickPayService) {
        if (quickPayService == null) {
            throw new IllegalArgumentException("QuickPayService cannot be null");
        }
        this.quickPayService = quickPayService;
    }

    @Override
    public PaymentResult processPayment(double amount) {
        if (amount <= 0.0) {
            return PaymentResult.failure("Payment failed: amount must be greater than zero");
        }

        QuickPayResponse response = quickPayService.pay(amount);
        if (response != null && response.getResponseCode() == 200) {
            return PaymentResult.success(
                    "QuickPay payment of INR " + amount + " processed successfully",
                    response.getReferenceNumber()
            );
        } else {
            return PaymentResult.failure("QuickPay rejected payment transaction");
        }
    }

    public QuickPayService getQuickPayService() {
        return quickPayService;
    }
}
