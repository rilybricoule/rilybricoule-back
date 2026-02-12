package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat,Long> {


    Optional<Chat> findByClientIdAndPrestataireIdAndReservationId(Long senderId, Long receiverId,Long reservationId);

}
