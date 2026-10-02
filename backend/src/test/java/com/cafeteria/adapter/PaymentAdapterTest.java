package com.cafeteria.adapter;

import com.cafeteria.adapter.external.MockPayService;
import com.cafeteria.adapter.external.QuickPayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentAdapterTest {

    @Test
    @DisplayName("MockPayAdapter successfully processes valid payment through PaymentProcessor interface")
    void testMockPayAdapterSuccessfulPayment() {
        MockPayService mockService = new MockPayService();
        PaymentProcessor processor = new MockPayAdapter(mockService);

        PaymentResult result = processor.processPayment(350.0);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "Payment should succeed for positive amount");
        assertNotNull(result.getTransactionId(), "Transaction ID must be generated");
        assertTrue(result.getTransactionId().startsWith("MPAY-INR-"), "Transaction ID format check");
        assertTrue(result.getMessage().contains("350.0"));
        assertSame(mockService, ((MockPayAdapter) processor).getMockPayService());
    }

    @Test
    @DisplayName("MockPayAdapter rejects zero and negative amounts")
    void testMockPayAdapterRejectsInvalidAmounts() {
        PaymentProcessor processor = new MockPayAdapter(new MockPayService());

        PaymentResult zeroResult = processor.processPayment(0.0);
        assertNotNull(zeroResult);
        assertFalse(zeroResult.isSuccess(), "Zero amount payment should fail");
        assertNull(zeroResult.getTransactionId());
        assertTrue(zeroResult.getMessage().contains("failed"));

        PaymentResult negativeResult = processor.processPayment(-50.0);
        assertNotNull(negativeResult);
        assertFalse(negativeResult.isSuccess(), "Negative amount payment should fail");
        assertNull(negativeResult.getTransactionId());
        assertTrue(negativeResult.getMessage().contains("failed"));
    }

    @Test
    @DisplayName("QuickPayAdapter successfully processes valid payment through PaymentProcessor interface")
    void testQuickPayAdapterSuccessfulPayment() {
        QuickPayService quickService = new QuickPayService();
        PaymentProcessor processor = new QuickPayAdapter(quickService);

        PaymentResult result = processor.processPayment(500.0);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "Payment should succeed for positive amount");
        assertNotNull(result.getTransactionId(), "Reference number must be generated");
        assertTrue(result.getTransactionId().startsWith("QP-REF-"), "Reference number format check");
        assertTrue(result.getMessage().contains("500.0"));
        assertSame(quickService, ((QuickPayAdapter) processor).getQuickPayService());
    }

    @Test
    @DisplayName("QuickPayAdapter rejects zero and negative amounts")
    void testQuickPayAdapterRejectsInvalidAmounts() {
        PaymentProcessor processor = new QuickPayAdapter(new QuickPayService());

        PaymentResult zeroResult = processor.processPayment(0.0);
        assertNotNull(zeroResult);
        assertFalse(zeroResult.isSuccess(), "Zero amount payment should fail");
        assertNull(zeroResult.getTransactionId());
        assertTrue(zeroResult.getMessage().contains("failed"));

        PaymentResult negativeResult = processor.processPayment(-100.0);
        assertNotNull(negativeResult);
        assertFalse(negativeResult.isSuccess(), "Negative amount payment should fail");
        assertNull(negativeResult.getTransactionId());
        assertTrue(negativeResult.getMessage().contains("failed"));
    }

    @Test
    @DisplayName("Polymorphic payment execution across different adapters using same Target interface")
    void testPolymorphicPaymentExecution() {
        PaymentProcessor[] processors = new PaymentProcessor[]{
                new MockPayAdapter(new MockPayService()),
                new QuickPayAdapter(new QuickPayService())
        };

        double paymentAmount = 250.0;

        for (PaymentProcessor processor : processors) {
            PaymentResult result = processor.processPayment(paymentAmount);
            assertNotNull(result);
            assertTrue(result.isSuccess(), "Each adapter should process valid payment polymorphically");
            assertNotNull(result.getTransactionId());
        }
    }

    @Test
    @DisplayName("Adapters throw IllegalArgumentException if null adaptee service is passed")
    void testNullAdapteeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new MockPayAdapter(null));
        assertThrows(IllegalArgumentException.class, () -> new QuickPayAdapter(null));
    }
}
