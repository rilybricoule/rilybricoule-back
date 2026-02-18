package com.sbsolutions.rilybricoule.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for payment requests.
 * Contains payment details for processing transactions.
 * SECURITY: Never expose payment token or sensitive payment information in responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    
    /**
     * Payment amount in the specified currency.
     * Business rule: Amount must be positive and match reservation total price.
     */
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Payment amount must have at most 10 digits and 2 decimal places")
    private BigDecimal amount;
    
    /**
     * ISO 4217 currency code (e.g., EUR, USD).
     * Business rule: Currency must be supported by the payment gateway.
     */
    @NotBlank(message = "Currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    private String currency;
    
    /**
     * Mock/Tokenized payment method identifier.
     * SECURITY: In production, this should represent a tokenized card/payment method,
     * never containing actual payment credentials.
     * Business rule: Token "fail" triggers payment failure simulation.
     */
    @NotBlank(message = "Payment method token is required")
    private String paymentMethodToken;
}
