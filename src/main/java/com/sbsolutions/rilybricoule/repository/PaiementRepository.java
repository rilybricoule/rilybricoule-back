package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    Optional<Paiement> findByReservationId(Long reservationId);
    Optional<Paiement> findByTransactionId(String transactionId);
}
