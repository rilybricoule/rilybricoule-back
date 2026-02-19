package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.CouponDTO;
import com.sbsolutions.rilybricoule.entity.Coupon;
import com.sbsolutions.rilybricoule.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service class for managing coupon operations.
 * 
 * Business responsibilities:
 * - Coupon validation (active status and expiry date)
 * - Coupon lookup by code and ID
 * - DTO mapping for API responses
 * 
 * Security considerations:
 * - Ensures only active, non-expired coupons are returned as valid
 * - Validates coupon state before use in reservations
 * 
 * @author RilyBricoule Backend Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {
    
    private final CouponRepository couponRepository;
    
    /**
     * Validate and retrieve a coupon by code.
     * 
     * Business rule: A coupon is valid if and only if:
     * 1. It exists in the database
     * 2. It is marked as active (active = true)
     * 3. Its expiry date is today or in the future
     * 
     * @param code the coupon code to search for
     * @return Optional containing the valid Coupon, or Empty if coupon does not exist or is invalid
     * @throws NullPointerException if code is null
     */
    public Optional<Coupon> findValidCoupon(String code) {
        Optional<Coupon> coupon = couponRepository.findByCode(code);
        
        if (coupon.isEmpty()) {
            return Optional.empty();
        }
        
        Coupon couponEntity = coupon.get();
        
        // Check if coupon is active and not expired
        if (!couponEntity.getActive() || couponEntity.getExpiryDate().isBefore(LocalDate.now())) {
            return Optional.empty();
        }
        
        return coupon;
    }
    
    /**
     * Find a coupon by its unique identifier.
     * 
     * @param id the coupon ID
     * @return Optional containing the Coupon with the given ID, or Empty if not found
     * @throws NullPointerException if id is null
     */
    public Optional<Coupon> findById(Long id) {
        return couponRepository.findById(id);
    }
    
    /**
     * Verify if a coupon is currently valid for use.
     * 
     * Business rule: A coupon is valid for use if:
     * 1. The coupon object is not null
     * 2. It is marked as active
     * 3. Its expiry date has not passed
     * 
     * @param coupon the coupon to validate
     * @return true if coupon is valid and usable, false otherwise
     */
    public boolean isValid(Coupon coupon) {
        if (coupon == null) {
            return false;
        }
        return coupon.getActive() && coupon.getExpiryDate().isAfter(LocalDate.now());
    }
    
    /**
     * Map Coupon entity to DTO for API responses.
     * 
     * Safe to expose all coupon fields in API responses as they do not contain sensitive information.
     * 
     * @param coupon the Coupon entity to convert
     * @return CouponDTO with all relevant fields, or null if coupon is null
     */
    public CouponDTO toDTO(Coupon coupon) {
        if (coupon == null) {
            return null;
        }
        return CouponDTO.builder()
            .id(coupon.getId())
            .code(coupon.getCode())
            .description(coupon.getDescription())
            .discountAmount(coupon.getDiscountAmount())
            .discountPercentage(coupon.getDiscountPercentage())
            .expiryDate(coupon.getExpiryDate())
            .active(coupon.getActive())
            .build();
    }
}
