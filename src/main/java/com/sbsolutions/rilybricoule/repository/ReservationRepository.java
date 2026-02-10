package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClientId(Long clientId);
    List<Reservation> findByPrestataireId(Long prestataireId);
    List<Reservation> findByReservationDate(LocalDate date);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    List<Reservation> findByClientIdAndStatus(Long clientId, Reservation.ReservationStatus status);
    List<Reservation> findByPrestataireIdAndStatus(Long prestataireId, Reservation.ReservationStatus status);
    boolean existsByClientIdAndPrestataireId(Long clientId, Long prestataireId);
}
