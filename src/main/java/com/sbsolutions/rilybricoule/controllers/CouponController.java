package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.CouponDTO;
import com.sbsolutions.rilybricoule.entity.Coupon;
import com.sbsolutions.rilybricoule.repository.CouponRepository;
import com.sbsolutions.rilybricoule.services.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    
    @PostMapping
    public ResponseEntity<CouponDTO> createCoupon(@RequestBody CouponDTO request) {
        Coupon coupon = Coupon.builder()
            .code(request.getCode())
            .description(request.getDescription())
            .discountAmount(request.getDiscountAmount())
            .discountPercentage(request.getDiscountPercentage())
            .expiryDate(request.getExpiryDate())
            .active(request.getActive() != null ? request.getActive() : true)
            .build();
        
        Coupon saved = couponRepository.save(coupon);
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.toDTO(saved));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCoupon(@PathVariable Long id) {
        Optional<Coupon> coupon = couponRepository.findById(id);
        if (coupon.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Coupon not found");
        }
        return ResponseEntity.ok(couponService.toDTO(coupon.get()));
    }
    
    @GetMapping
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        List<CouponDTO> coupons = couponRepository.findAll()
            .stream()
            .map(couponService::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(coupons);
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getCouponByCode(@PathVariable String code) {
        Optional<Coupon> coupon = couponService.findValidCoupon(code);
        if (coupon.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Valid coupon not found or expired");
        }
        return ResponseEntity.ok(couponService.toDTO(coupon.get()));
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<CouponDTO>> getActiveCoupons() {
        List<CouponDTO> coupons = couponRepository.findByActiveTrueOrderByExpiryDateDesc()
            .stream()
            .map(couponService::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(coupons);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable Long id, @RequestBody CouponDTO request) {
        Optional<Coupon> couponOpt = couponRepository.findById(id);
        if (couponOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Coupon not found");
        }
        
        Coupon coupon = couponOpt.get();
        coupon.setCode(request.getCode());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountAmount(request.getDiscountAmount());
        coupon.setDiscountPercentage(request.getDiscountPercentage());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(request.getActive());
        
        Coupon updated = couponRepository.save(coupon);
        return ResponseEntity.ok(couponService.toDTO(updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        if (!couponRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Coupon not found");
        }
        couponRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
