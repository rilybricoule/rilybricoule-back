package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.exceptions.PaymentFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API.
 * 
 * Provides centralized exception handling and customized error responses
 * for all REST endpoints. Ensures consistent API error format across all endpoints.
 * 
 * Handles:
 * - MethodArgumentNotValidException: Request body validation errors (400)
 * - IllegalArgumentException: Business logic errors (400)
 * - PaymentFailedException: Payment processing errors (402)
 * - Generic Exception: Unexpected errors (500)
 * 
 * Response format includes:
 * - timestamp: When the error occurred
 * - status: HTTP status code
 * - error: Error type/message
 * - message: Detailed error description
 * - details: Field-specific validation errors (if applicable)
 * - path: The request URL path
 * 
 * @author RilyBricoule Backend Team
 * @version 1.0
 */
@ControllerAdvice
@Slf4j
public class RestExceptionHandler {

    /**
     * Handle request body validation errors (MethodArgumentNotValidException).
     * 
     * Called when @Valid annotation fails on request body validation.
     * Collects all field-level validation errors and returns them in a structured format.
     * 
     * HTTP Status: 400 Bad Request
     * 
     * @param ex the MethodArgumentNotValidException containing validation errors
     * @param request the current web request
     * @return ResponseEntity with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        // Collect field validation errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        response.put("message", "Request validation failed");
        response.put("details", fieldErrors);
        response.put("path", request.getDescription(false).replace("uri=", ""));

        log.warn("Validation error: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle business logic errors (IllegalArgumentException).
     * 
     * Thrown when business rules are violated, such as:
     * - Entity not found (client, prestataire, coupon, reservation)
     * - Invalid entity references
     * - Invalid status transitions
     * 
     * HTTP Status: 400 Bad Request
     * 
     * @param ex the IllegalArgumentException with error details
     * @param request the current web request
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException ex,
            WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        log.warn("Business logic error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle payment processing errors (PaymentFailedException).
     * 
     * Thrown when payment processing fails through the payment gateway.
     * This is a specific business exception indicating payment-related issues.
     * 
     * HTTP Status: 402 Payment Required
     * 
     * @param ex the PaymentFailedException with payment error details
     * @param request the current web request
     * @return ResponseEntity with payment error details
     */
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentFailed(
            PaymentFailedException ex,
            WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", 402); // Payment Required
        response.put("error", "Payment Failed");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        log.error("Payment error: {}", ex.getMessage());
        return ResponseEntity.status(402).body(response);
    }

    /**
     * Handle unexpected/generic exceptions.
     * 
     * Catches all other exceptions that are not handled by specific handlers.
     * Logs the full exception for debugging purposes.
     * 
     * HTTP Status: 500 Internal Server Error
     * 
     * @param ex the unexpected Exception
     * @param request the current web request
     * @return ResponseEntity with generic error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex,
            WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");
        response.put("path", request.getDescription(false).replace("uri=", ""));

        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
