package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.DispatchStatus;
import com.sbsolutions.rilybricoule.entity.ReservationDispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationDispatchRepository extends JpaRepository<ReservationDispatch, Long> {

    List<ReservationDispatch> findByReservation_Id(Long reservationId);

    List<ReservationDispatch> findByReservation_IdAndStatus(Long reservationId, DispatchStatus status);

    Optional<ReservationDispatch> findByReservation_IdAndPrestataire_Id(Long reservationId, Long prestataireId);

    boolean existsByReservation_IdAndStatus(Long reservationId, DispatchStatus status);

    boolean existsByReservation_IdAndPrestataire_Id(Long reservationId, Long prestataireId);

    List<ReservationDispatch> findByPrestataire_Id(Long prestataireId);

    @Query("""
    SELECT rd.prestataire.id
    FROM ReservationDispatch rd
    WHERE rd.reservation.id = :reservationId
""")
    List<Long> findPrestataireIdsByReservationId(@Param("reservationId") Long reservationId);

    long countByReservation_IdAndStatus(Long reservationId, DispatchStatus status);

    @Query("""
    SELECT MAX(rd.sentAt)
    FROM ReservationDispatch rd
    WHERE rd.reservation.id = :reservationId
""")
    LocalDateTime findLastSentAtByReservationId(@Param("reservationId") Long reservationId);

    @Query("""
    SELECT rd.prestataire.id
    FROM ReservationDispatch rd
    WHERE rd.reservation.id = :reservationId
      AND rd.status = :status
""")
    List<Long> findPrestataireIdsByReservationIdAndStatus(@Param("reservationId") Long reservationId,
                                                          @Param("status") DispatchStatus status);

}