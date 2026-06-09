package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClient_Id(Long clientId); // note _Id
    List<Reservation> findByPrestataire_Id(Long prestataireId); // corrected
    List<Reservation> findByReservationDate(LocalDate date);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    List<Reservation> findByClient_IdAndStatus(Long clientId, Reservation.ReservationStatus status); // corrected
    List<Reservation> findByPrestataire_IdAndStatus(Long prestataireId, Reservation.ReservationStatus status); // corrected
    boolean existsByClient_IdAndPrestataire_Id(Long clientId, Long prestataireId);

    @Query("""
    SELECT r.prestataire.id, COUNT(r)
    FROM Reservation r
    WHERE r.status = 'CONFIRMED'
      AND r.prestataire.id IN :ids
    GROUP BY r.prestataire.id
""")
    List<Object[]> countActiveReservationsByPrestataireIds(@Param("ids") List<Long> ids);

    @Query("""
    SELECT r.prestataire.id, COUNT(r)
    FROM Reservation r
    WHERE r.status = 'COMPLETED'
      AND r.prestataire.id IN :ids
    GROUP BY r.prestataire.id
""")
    List<Object[]> countCompletedReservationsByPrestataireIds(@Param("ids") List<Long> ids);


}
