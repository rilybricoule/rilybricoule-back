package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.CouponDTO;
import com.sbsolutions.rilybricoule.entity.Coupon;
import com.sbsolutions.rilybricoule.mapper.CouponMapper;
import com.sbsolutions.rilybricoule.repository.CouponRepository;
import com.sbsolutions.rilybricoule.services.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST API Controller for coupon management.
 * 
 * Endpoints:
 * - POST /api/coupons - Create a new coupon
 * - GET /api/coupons - Get all coupons
 * - GET /api/coupons/{id} - Get coupon by ID
 * - GET /api/coupons/code/{code} - Get valid coupon by code
 * - GET /api/coupons/active - Get all active coupons
 * - PUT /api/coupons/{id} - Update a coupon
 * - DELETE /api/coupons/{id} - Delete a coupon
 * 
 * Uses DTOs for request/response to ensure API consistency and security.
 * Request body validation is automatically enforced via @Valid annotation.
 */
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final CouponMapper couponMapper;
    
    /**
     * Create a new coupon.
     * 
     * @param request the CouponDTO with coupon data (validated)
     * @return ResponseEntity with created CouponDTO and 201 CREATED status
     */
    @PostMapping
    public ResponseEntity<CouponDTO> createCoupon(@Valid @RequestBody CouponDTO request) {
        Coupon coupon = couponMapper.toEntity(request);
        Coupon saved = couponRepository.save(coupon);
        return ResponseEntity.status(HttpStatus.CREATED).body(couponMapper.toDTO(saved));
    }
    
    /**
     * Get a coupon by ID.
     * 
     * @param id the coupon ID
     * @return ResponseEntity with CouponDTO if found, 404 NOT_FOUND otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCoupon(@PathVariable Long id) {
        Optional<Coupon> coupon = couponRepository.findById(id);
        if (coupon.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Coupon not found");
        }
        return ResponseEntity.ok(couponMapper.toDTO(coupon.get()));
    }
    
    /**
     * Get all coupons.
     * 
     * @return ResponseEntity with list of all CouponDTOs
     */
    @GetMapping
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        List<CouponDTO> coupons = couponRepository.findAll()
            .stream()
            .map(couponMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(coupons);
    }
    
    /**
     * Get a valid (active and not expired) coupon by code.
     * 
     * @param code the coupon code
     * @return ResponseEntity with CouponDTO if valid, 404 NOT_FOUND otherwise
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getCouponByCode(@PathVariable String code) {
        Optional<Coupon> coupon = couponService.findValidCoupon(code);
        if (coupon.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Valid coupon not found or expired");
        }
        return ResponseEntity.ok(couponMapper.toDTO(coupon.get()));
    }
    
    /**
     * Get all active coupons (sorted by expiry date, newest first).
     * 
     * @return ResponseEntity with list of active CouponDTOs
     */
    @GetMapping("/active")
    public ResponseEntity<List<CouponDTO>> getActiveCoupons() {
        List<CouponDTO> coupons = couponRepository.findByActiveTrueOrderByExpiryDateDesc()
            .stream()
            .map(couponMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(coupons);
    }
    
    /**
     * Update a coupon.
     * 
     * @param id the coupon ID
     * @param request the CouponDTO with updated data (validated)
     * @return ResponseEntity with updated CouponDTO if found, 404 NOT_FOUND otherwise
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponDTO request) {
        Optional<Coupon> couponOpt = couponRepository.findById(id);
        if (couponOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Coupon not found");
        }
        
        Coupon coupon = couponOpt.get();
        couponMapper.updateEntity(request, coupon);
        Coupon updated = couponRepository.save(coupon);
        return ResponseEntity.ok(couponMapper.toDTO(updated));
    }
    
    /**
     * Delete a coupon.
     * 
     * @param id the coupon ID
     * @return ResponseEntity with 204 NO_CONTENT if deleted, 404 NOT_FOUND otherwise
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        if (!couponRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Coupon not found");
        }
        couponRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
