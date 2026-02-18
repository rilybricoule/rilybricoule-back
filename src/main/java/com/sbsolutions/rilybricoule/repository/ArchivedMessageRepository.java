package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.ArchivedMessages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchivedMessageRepository extends JpaRepository<ArchivedMessages, Long> {

    List<ArchivedMessages> findByChatIdOrderByCreatedAtAsc(Long chatId);
}