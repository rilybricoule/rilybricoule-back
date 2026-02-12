package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
