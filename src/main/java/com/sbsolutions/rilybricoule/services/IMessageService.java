package com.sbsolutions.rilybricoule.services;


import com.sbsolutions.rilybricoule.dto.input.MessageInputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.entity.Chat;
import com.sbsolutions.rilybricoule.entity.Message;
import com.sbsolutions.rilybricoule.entity.User;
import org.hibernate.query.internal.FetchMementoEmbeddableStandard;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IMessageService {

    MessageOutputDto sendMessage(Long chatId, Long senderId, String content);
     MessageOutputDto saveMessage(Long chat, Long senderId, MessageInputDto content);


    List<MessageOutputDto> getMessagesByChatId(Long chatId);
}
