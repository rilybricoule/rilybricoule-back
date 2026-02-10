package com.sbsolutions.rilybricoule.services;


import com.sbsolutions.rilybricoule.entity.Chat;
import com.sbsolutions.rilybricoule.entity.Message;
import com.sbsolutions.rilybricoule.entity.User;

import java.util.List;

public interface IMessageService {

    List<Message> getMessagesByChatId(Long chatId);
    Message sendMessage(Long chatId, User sender, String content);
     Message saveMessage(Chat chat, User sender, String content);
}
