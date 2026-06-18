package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
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

    long countByPrestataire_IdAndStatus(
            Long prestataireId,
            Reservation.ReservationStatus status
    );


    long countByStatusIn(Collection<Reservation.ReservationStatus> statuses);

    long countByStatusAndReservationDate(
            Reservation.ReservationStatus status,
            LocalDate reservationDate
    );



    long countByClient_Id(Long clientId);

    long countByClient_IdAndStatus(
            Long clientId,
            Reservation.ReservationStatus status
    );

    Optional<Reservation> findTopByClient_IdOrderByReservationDateDesc(Long clientId);




}
