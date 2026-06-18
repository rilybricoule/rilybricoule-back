package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AdminPromoDTO;
import com.sbsolutions.rilybricoule.entity.Coupon;
import com.sbsolutions.rilybricoule.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminPromoService {

    private final CouponRepository couponRepository;

    public List<AdminPromoDTO> getAll() {
        return couponRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public AdminPromoDTO create(AdminPromoDTO request) {
        if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code already exists");
        }

        Coupon coupon = new Coupon();

        applyRequest(coupon, request);
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setCurrentUsage(0);

        return toDTO(couponRepository.save(coupon));
    }

    public AdminPromoDTO update(Long id, AdminPromoDTO request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Promo not found"
                ));

        applyRequest(coupon, request);

        return toDTO(couponRepository.save(coupon));
    }

    public void delete(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Promo not found");
        }

        couponRepository.deleteById(id);
    }

    public AdminPromoDTO updateStatus(Long id, boolean active) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Promo not found"
                ));

        coupon.setActive(active);

        return toDTO(couponRepository.save(coupon));
    }

    private void applyRequest(Coupon coupon, AdminPromoDTO request) {
        coupon.setCode(request.getCode());
        coupon.setTitle(request.getTitle());
        coupon.setDescription(request.getDescription());

        coupon.setDiscountAmount(
                request.getDiscountAmount() == null
                        ? BigDecimal.ZERO
                        : request.getDiscountAmount()
        );

        coupon.setDiscountPercentage(
                request.getDiscountPercentage() == null
                        ? 0
                        : request.getDiscountPercentage()
        );

        coupon.setStartDate(
                request.getStartDate() == null
                        ? LocalDate.now()
                        : request.getStartDate()
        );

        coupon.setExpiryDate(request.getEndDate());

        coupon.setTargetAudience(request.getTargetAudience());
        coupon.setMaxUsage(request.getMaxUsage());

        coupon.setActive(
                request.getActive() == null
                        || request.getActive()
        );
    }

    private AdminPromoDTO toDTO(Coupon coupon) {
        AdminPromoDTO dto = new AdminPromoDTO();

        dto.setId(coupon.getId());
        dto.setCode(coupon.getCode());
        dto.setTitle(coupon.getTitle() != null ? coupon.getTitle() : coupon.getCode());
        dto.setDescription(coupon.getDescription());
        dto.setDiscountAmount(coupon.getDiscountAmount());
        dto.setDiscountPercentage(coupon.getDiscountPercentage());
        dto.setStartDate(coupon.getStartDate());
        dto.setEndDate(coupon.getExpiryDate());
        dto.setTargetAudience(coupon.getTargetAudience());
        dto.setMaxUsage(coupon.getMaxUsage());
        dto.setCurrentUsage(coupon.getCurrentUsage());
        dto.setActive(coupon.getActive());
        dto.setCreatedAt(coupon.getCreatedAt());

        return dto;
    }
}