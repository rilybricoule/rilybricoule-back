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
    List<Reservation> findByPrestaireId(Long prestaireId);
    List<Reservation> findByReservationDate(LocalDate date);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    List<Reservation> findByClientIdAndStatus(Long clientId, Reservation.ReservationStatus status);
    List<Reservation> findByPrestaireIdAndStatus(Long prestaireId, Reservation.ReservationStatus status);
    boolean existsByClientIdAndPrestaireId(Long clientId, Long prestaireId);
}
