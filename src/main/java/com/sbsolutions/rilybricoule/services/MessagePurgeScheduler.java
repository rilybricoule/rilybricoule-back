package com.sbsolutions.rilybricoule.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePurgeScheduler {

    private final IMessageService messageService;

    // Every day at 03:00
    @Scheduled(cron = "0 0 3 * * *", zone = "Africa/Casablanca")
    public void purgeOldDeletedMessages() {
        int count = messageService.purgeDeletedMessagesOlderThanSevenDays();
        log.info("Scheduled purge completed. Deleted {} messages.", count);
    }
}