package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat,Long> {


    Optional<Chat> findByClientIdAndPrestataireIdAndReservationId(Long senderId, Long receiverId,Long reservationId);



    @Query("""
SELECT c FROM Chat c
WHERE c.active = true
AND c.archivedAt IS NULL
AND (
    (
        c.client IS NOT NULL
        AND c.client.id = :userId
        AND c.deletedByClientAt IS NULL
    )
    OR
    (
        c.prestataire IS NOT NULL
        AND c.prestataire.id = :userId
        AND c.deletedByPrestataireAt IS NULL
    )
    OR
    (
        c.participantOne IS NOT NULL
        AND c.participantOne.id = :userId
    )
    OR
    (
        c.participantTwo IS NOT NULL
        AND c.participantTwo.id = :userId
    )
)
ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC
""")
    List<Chat> findActivechatsByUserId(@Param("userId") Long userId);

    @Query("""
SELECT c FROM Chat c
WHERE (
    c.participantOne.id = :userOneId
    AND c.participantTwo.id = :userTwoId
)
OR (
    c.participantOne.id = :userTwoId
    AND c.participantTwo.id = :userOneId
)
""")
    Optional<Chat> findGenericChat(
            @Param("userOneId") Long userOneId,
            @Param("userTwoId") Long userTwoId
    );
    // Add this method (for when you list "my chats" — only non-archived)

}
