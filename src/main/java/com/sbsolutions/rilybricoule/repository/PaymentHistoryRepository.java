package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    List<PaymentHistory> findByReservationId(Long reservationId);
}
