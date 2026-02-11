package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.entity.Chat;
import com.sbsolutions.rilybricoule.entity.Message;
import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.repository.ChatRepository;
import com.sbsolutions.rilybricoule.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService{

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    @Override
    public Message sendMessage(Long chatId, User sender, String content) {

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.isActive()) {
            throw new IllegalStateException("Chat is closed");
        }

        return saveMessage(chat, sender, content);
    }


    public Message saveMessage(Chat chat, User sender, String content) {

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .content(content)
                .sentAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }


    @Override
    public List<Message> getMessagesByChatId(Long chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }
}






