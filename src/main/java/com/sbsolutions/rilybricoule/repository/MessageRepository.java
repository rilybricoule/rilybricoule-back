package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {

    List<Message> findByChatIdOrderByCreatedAtAsc(Long chatId);



    @Query("""
       SELECT m FROM Message m
       WHERE (m.sender.id = :user1 AND m.receiver.id = :user2)
          OR (m.sender.id = :user2 AND m.receiver.id = :user1)
       ORDER BY m.createdAt ASC
       """)
    List<Message> findConversation(Long user1, Long user2);

}
