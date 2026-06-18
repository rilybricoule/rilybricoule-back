package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);

    List<Coupon> findTop5ByOrderByCreatedAtDesc();

    List<Coupon> findByActiveTrueOrderByExpiryDateDesc();


    List<Coupon> findAllByOrderByCreatedAtDesc();

    boolean existsByCodeIgnoreCase(String code);
}
