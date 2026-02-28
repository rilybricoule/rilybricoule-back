package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {

    @Modifying
    @Query("""
       UPDATE Message m 
       SET m.read = true, m.readAt = CURRENT_TIMESTAMP 
       WHERE m.chat.id = :chatId 
       AND m.receiver.id = :receiverId 
       AND m.read = false
       """)
    void markMessagesAsRead(Long chatId, Long receiverId);

    @Query("SELECT m FROM Message m WHERE m.deleted = true AND m.deletedAt < :before")
    List<Message> findByDeletedTrueAndDeletedAtBefore(@Param("before") LocalDateTime before);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.deleted = true AND m.deletedAt > :after")
    List<Message> findByChatIdAndDeletedTrueAndDeletedAtAfter(@Param("chatId") Long chatId, @Param("after") LocalDateTime after);

    // Active messages only (not deleted, not archived)
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.deleted = false AND m.archivedAt IS NULL ORDER BY m.createdAt ASC")
    List<Message> findActiveByChatIdOrderByCreatedAtAsc(@Param("chatId") Long chatId);



    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.read = false AND m.deleted = false AND  m.archivedAt IS NULL")
    long countUnreadByReceiverId(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.createdAt < :before AND m.deleted = false AND m.archivedAt IS NULL")
    List<Message> findToArchiveByCreatedAtBefore(@Param("before") LocalDateTime before);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.receiver.id = :userId AND m.read = false AND m.deleted = false AND m.archivedAt IS NULL")
    long countUnreadByChatIdAndReceiverId(@Param("chatId") Long chatId, @Param("userId") Long userId);

    @Query("""
       SELECT m FROM Message m
       WHERE (m.sender.id = :user1 AND m.receiver.id = :user2)
          OR (m.sender.id = :user2 AND m.receiver.id = :user1)
       ORDER BY m.createdAt ASC
       """)
    List<Message> findConversation(Long user1, Long user2);


    @Modifying
    @Query("UPDATE Message m SET m.archivedAt = CURRENT_TIMESTAMP WHERE m.createdAt < :before AND m.deleted = false AND m.archivedAt IS NULL")
    int setArchivedAtForOlderThan(@Param("before") LocalDateTime before);
}
