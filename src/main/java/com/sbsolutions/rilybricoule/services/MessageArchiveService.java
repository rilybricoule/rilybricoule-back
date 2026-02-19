package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageArchiveService {

    private static final int ARCHIVE_AFTER_MONTHS = 6;

    private final MessageRepository messageRepository;

    @Scheduled(cron = "0 0 2 * * ?")  // Every day at 2:00 AM
    @Transactional
    public int archiveOldMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(ARCHIVE_AFTER_MONTHS);
        int count = messageRepository.setArchivedAtForOlderThan(threshold);
        if (count > 0) {
            log.info("Archived {} messages older than {} months", count, ARCHIVE_AFTER_MONTHS);
        }
        return count;
    }


    @Transactional
    public int archiveOlderThanMinutes(int olderThanMinutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(olderThanMinutes);
        return messageRepository.setArchivedAtForOlderThan(threshold);
    }
}