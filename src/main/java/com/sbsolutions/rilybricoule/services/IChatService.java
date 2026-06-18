package com.sbsolutions.rilybricoule.services;



import com.sbsolutions.rilybricoule.dto.output.ChatOutputDto;
import com.sbsolutions.rilybricoule.entity.Chat;
import com.sbsolutions.rilybricoule.entity.ChatType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IChatService {

    Chat startOrGetChat(Long clientId, Long prestataireId, Long reservationId);

    void pinConversation(Long chatId, Long userId);
    void unpinConversation(Long chatId, Long userId);

    void archiveConversation(Long chatId, Long userId);

    void disableConversation(Long chatId, Long userId);

    void enableConversation(Long chatId, Long userId);

    void restoreDeletedConversation(Long chatId, Long userId);

    void deleteConversation(Long chatId, Long userId);

    void unarchiveConversation(Long chatId, Long userId);

    List<ChatOutputDto> getChatsByUserId(Long userId);

    ChatOutputDto openGenericChat(Long userOneId, Long userTwoId, ChatType type);
}
