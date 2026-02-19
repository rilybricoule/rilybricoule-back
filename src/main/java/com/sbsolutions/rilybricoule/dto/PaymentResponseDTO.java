package com.sbsolutions.rilybricoule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for payment responses.
 * Contains only non-sensitive payment information for client consumption.
 * SECURITY: This DTO never exposes payment credentials, tokens, or full transaction details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    
    /**
     * Payment operation success status.
     * Business rule: true indicates payment was successfully processed.
     */
    @JsonProperty("success")
    private boolean success;
    
    /**
     * Unique transaction identifier.
     * Business rule: Used for referencing and reconciling transactions.
     * SECURITY: This is a safe identifier and does not contain sensitive payment information.
     */
    @NotBlank(message = "Transaction ID is required on success")
    private String transactionId;
    
    /**
     * User-friendly message describing the payment result.
     * Business rule: Can be safely displayed to the client.
     */
    private String message;
}
