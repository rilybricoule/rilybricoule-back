package com.sbsolutions.rilybricoule.mapper;

import com.sbsolutions.rilybricoule.dto.PaymentRequestDTO;
import com.sbsolutions.rilybricoule.dto.PaymentResponseDTO;
import com.sbsolutions.rilybricoule.entity.Paiement;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Payment entity and Payment DTOs.
 * Ensures sensitive payment information is never exposed in API responses.
 * Handles mapping for payment requests and responses separately.
 */
@Component
public class PaymentMapper {

    /**
     * Convert Paiement entity to PaymentResponseDTO.
     * SECURITY: Never includes sensitive payment information like paymentMethodToken.
     * Only exposes safe transaction identifiers and status information.
     * 
     * @param paiement the Paiement entity
     * @return PaymentResponseDTO with safe fields only, or null if input is null
     */
    public PaymentResponseDTO toResponseDTO(Paiement paiement) {
        if (paiement == null) {
            return null;
        }

        return PaymentResponseDTO.builder()
            .success(isPaymentSuccessful(paiement))
            .transactionId(paiement.getTransactionId())
            .message(buildStatusMessage(paiement))
            .build();
    }

    /**
     * Build a user-friendly message based on payment status.
     * 
     * @param paiement the payment entity
     * @return appropriate message for the current status
     */
    private String buildStatusMessage(Paiement paiement) {
        if (paiement.getPaymentStatus() == null) {
            return "Payment status is unknown";
        }

        return switch (paiement.getPaymentStatus()) {
            case SUCCESS -> "Payment processed successfully";
            case PENDING -> "Payment is pending";
            case FAILED -> "Payment processing failed";
            case REFUNDED -> "payment has benn refunded";
        };
    }

    /**
     * Check if payment was successful.
     * 
     * @param paiement the payment entity
     * @return true if payment status is SUCCESS, false otherwise
     */
    private boolean isPaymentSuccessful(Paiement paiement) {
        return paiement.getPaymentStatus() == Paiement.PaymentStatus.SUCCESS;
    }

    /**
     * Convert PaymentRequestDTO to Paiement entity.
     * Creates a new pending payment entity from request data.
     * Note: This method does not set relationships which must be configured separately.
     * 
     * @param dto the PaymentRequestDTO with payment details
     * @return Paiement entity with PENDING status, or null if input is null
     */
    public Paiement toEntity(PaymentRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Paiement.builder()
            .amount(dto.getAmount())
            .paymentStatus(Paiement.PaymentStatus.PENDING)
            .paymentMode("CARD") // Default payment mode
            .build();
        // Note: transactionId and reservation must be set separately
    }

    /**
     * Update existing Paiement entity with response data after payment processing.
     * Sets transaction ID, payment date, and status based on response.
     * 
     * @param paymentResponse the payment response data
     * @param paiement the existing payment entity to update
     * @param transactionId the transaction identifier from payment gateway
     * @return the updated Paiement entity
     */
    public Paiement updateWithResponse(PaymentResponseDTO paymentResponse, Paiement paiement, String transactionId) {
        if (paymentResponse == null) {
            return paiement;
        }

        paiement.setTransactionId(transactionId);
        paiement.setPaymentStatus(
            paymentResponse.isSuccess() ? Paiement.PaymentStatus.SUCCESS : Paiement.PaymentStatus.FAILED
        );

        return paiement;
    }
}
