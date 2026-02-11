package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {
    Optional<Avis> findByReservationId(Long reservationId);
    List<Avis> findByPrestataireId(Long prestataireId);
    List<Avis> findByPrestataireIdOrderByCreatedDateDesc(Long prestataireId);
}
