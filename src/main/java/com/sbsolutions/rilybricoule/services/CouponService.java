package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.CouponDTO;
import com.sbsolutions.rilybricoule.entity.Coupon;
import com.sbsolutions.rilybricoule.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {
    
    private final CouponRepository couponRepository;
    
    /**
     * Validate and retrieve a coupon by code.
     * A coupon is valid if it exists, is active, and has not expired.
     */
    public Optional<Coupon> findValidCoupon(String code) {
        Optional<Coupon> coupon = couponRepository.findByCode(code);
        
        if (coupon.isEmpty()) {
            return Optional.empty();
        }
        
        Coupon couponEntity = coupon.get();
        if (!couponEntity.getActive() || couponEntity.getExpiryDate().isBefore(LocalDate.now())) {
            return Optional.empty();
        }
        
        return coupon;
    }
    
    /**
     * Find a coupon by ID.
     */
    public Optional<Coupon> findById(Long id) {
        return couponRepository.findById(id);
    }
    
    /**
     * Check if a coupon is valid (active and not expired).
     */
    public boolean isValid(Coupon coupon) {
        if (coupon == null) {
            return false;
        }
        return coupon.getActive() && coupon.getExpiryDate().isAfter(LocalDate.now());
    }
    
    /**
     * Map Coupon entity to DTO.
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
