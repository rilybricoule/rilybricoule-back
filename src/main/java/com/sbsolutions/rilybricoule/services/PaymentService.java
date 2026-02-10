package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.PaymentRequestDTO;
import com.sbsolutions.rilybricoule.dto.PaymentResponseDTO;
import com.sbsolutions.rilybricoule.exceptions.PaymentFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    /**
     * Mock payment processor.
     * - If `paymentMethodToken` equals "fail" then simulate a failure.
     * - Otherwise return a successful PaymentResponseDTO.
     */
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        if (request == null || request.getAmount() == null) {
            throw new IllegalArgumentException("Payment request or amount is missing");
        }

        String token = request.getPaymentMethodToken();
        if (token != null && token.equalsIgnoreCase("fail")) {
            throw new PaymentFailedException("Payment was declined by the payment provider");
        }

        // Simulate processing delay / call to external gateway
        String transactionId = UUID.randomUUID().toString();

        return PaymentResponseDTO.builder()
            .success(true)
            .transactionId(transactionId)
            .message("Mock payment succeeded")
            .build();
    }
}
