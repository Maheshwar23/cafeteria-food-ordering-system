package com.cafeteria.adapter.external;

import java.util.UUID;

/**
 * Mock third-party payment service "QuickPay".
 * Incompatible API: exposes pay(double amountInRupees) returning a custom QuickPayResponse.
 * Does NOT implement PaymentProcessor.
 */
public class QuickPayService {

    /**
     * Vendor-specific response structure.
     */
    public static class QuickPayResponse {
        private final int responseCode; // 200 = Success, 400 = Failure
        private final String referenceNumber;

        public QuickPayResponse(int responseCode, String referenceNumber) {
            this.responseCode = responseCode;
            this.referenceNumber = referenceNumber;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public String getReferenceNumber() {
            return referenceNumber;
        }
    }

    /**
     * Vendor-specific payment method.
     *
     * @param amountInRupees payment amount
     * @return QuickPayResponse containing response code and reference number
     */
    public QuickPayResponse pay(double amountInRupees) {
        if (amountInRupees <= 0.0) {
            return new QuickPayResponse(400, null);
        }
        return new QuickPayResponse(200, "QP-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
}
