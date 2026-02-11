package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.exceptions.PaymentFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + ex.getMessage());
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<?> handlePaymentFailed(PaymentFailedException ex) {
        return ResponseEntity.status(402).body("Payment Required: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
    }
}
