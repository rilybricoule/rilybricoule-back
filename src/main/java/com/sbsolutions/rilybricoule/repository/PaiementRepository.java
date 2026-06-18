package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    Optional<Paiement> findByReservationId(Long reservationId);
    Optional<Paiement> findByTransactionId(String transactionId);

    List<Paiement> findAllByOrderByCreatedAtDesc();

    List<Paiement> findAllByReservation_IdOrderByCreatedAtDesc(Long reservationId);


    long countByPaymentStatus(Paiement.PaymentStatus status);

    @Query("""
    select coalesce(sum(p.amount), 0)
    from Paiement p
    where p.paymentStatus = :status
      and p.paymentDate >= :start
      and p.paymentDate < :end
""")
    BigDecimal sumAmountByStatusBetweenDates(
            @Param("status") Paiement.PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
