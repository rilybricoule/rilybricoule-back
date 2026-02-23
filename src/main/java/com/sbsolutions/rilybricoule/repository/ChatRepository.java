package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat,Long> {


    Optional<Chat> findByClientIdAndPrestataireIdAndReservationId(Long senderId, Long receiverId,Long reservationId);


    // Add this method (for when you list "my chats" — only non-archived)
    @Query("SELECT c FROM Chat c WHERE (c.client.id = :userId OR c.prestataire.id = :userId) AND c.archivedAt IS NULL")
    List<Chat> findActiveByUserId(@Param("userId") Long userId);

    // Optional: list only archived chats for a user
    @Query("SELECT c FROM Chat c WHERE (c.client.id = :userId OR c.prestataire.id = :userId) AND c.archivedAt IS NOT NULL")
    List<Chat> findArchivedByUserId(@Param("userId") Long userId);
}
