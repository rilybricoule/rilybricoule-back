package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.MessageInputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;

import java.util.List;

public interface IMessageService {

    MessageOutputDto sendMessage(Long chatId, Long senderId, MessageInputDto inputDto);

    MessageOutputDto saveMessage(Long chatId, Long senderId, MessageInputDto inputDto);

    MessageOutputDto editMessage(Long messageId, Long userId, String newContent);

    void deleteMessage(Long messageId, Long userId);

    List<MessageOutputDto> getMessagesByChatId(Long chatId);

    void markAsRead(Long chatId, Long receiverId);

    void restoreConversation(Long chatId, Long userId);

    long getUnreadMessageCount(Long userId);

    long getUnreadMessageCountForChat(Long chatId, Long userId);

    int purgeDeletedMessagesOlderThanSevenDays();

}
