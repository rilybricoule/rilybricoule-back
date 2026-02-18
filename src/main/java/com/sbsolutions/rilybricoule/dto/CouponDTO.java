package com.sbsolutions.rilybricoule.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Coupon entity.
 * Used for both request and response in coupon-related endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDTO {
    
    /**
     * Unique identifier for the coupon. 
     * Only populated in response DTOs.
     */
    private Long id;
    
    /**
     * Unique coupon code.
     * Business rule: Must be unique and used to identify coupons in the system.
     */
    @NotBlank(message = "Coupon code cannot be blank")
    @Size(min = 3, max = 100, message = "Coupon code must be between 3 and 100 characters")
    private String code;
    
    /**
     * Description of the coupon discount.
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    /**
     * Fixed discount amount in currency units.
     * Business rule: Either discountAmount or discountPercentage must be set.
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount amount must be zero or positive")
    @Digits(integer = 12, fraction = 2, message = "Discount amount must have at most 10 digits and 2 decimal places")
    private BigDecimal discountAmount;
    
    /**
     * Discount percentage (0-100).
     * Business rule: Either discountAmount or discountPercentage must be set.
     */
    @Min(value = 0, message = "Discount percentage must be at least 0")
    @Max(value = 100, message = "Discount percentage cannot exceed 100")
    private Integer discountPercentage;
    
    /**
     * Expiry date for the coupon.
     * Business rule: Expired coupons are automatically considered invalid.
     */
    @NotNull(message = "Expiry date is required")
    @FutureOrPresent(message = "Expiry date must be today or in the future")
    private LocalDate expiryDate;
    
    /**
     * Active status of the coupon.
     * Business rule: Only active coupons can be applied to reservations.
     */
    private Boolean active;
}
