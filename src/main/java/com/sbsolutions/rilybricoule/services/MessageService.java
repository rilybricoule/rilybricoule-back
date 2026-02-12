package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.MessageInputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.entity.Chat;
import com.sbsolutions.rilybricoule.entity.Message;
import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.mapper.MessageMapper;
import com.sbsolutions.rilybricoule.repository.ChatRepository;
import com.sbsolutions.rilybricoule.repository.MessageRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final INotificationService notificationService;

    // ------------------- SEND MESSAGE -------------------
    @Override
    public MessageOutputDto sendMessage(Long chatId, Long senderId, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.isActive()) {
            throw new IllegalStateException("Chat is closed");
        }

        // Save message using senderId
        MessageInputDto inputDto = new MessageInputDto();
        inputDto.setChatId(chatId);
        inputDto.setSenderId(senderId);
        inputDto.setContent(content);

        return saveMessage(chatId, senderId, inputDto);
    }

    // ------------------- SAVE MESSAGE -------------------
    @Override
    public MessageOutputDto saveMessage(Long chatId, Long senderId, MessageInputDto inputDto) {
        // Fetch the chat by ID
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found with id " + chatId));

        // Fetch the sender
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found with id " + senderId));

        // Determine the receiver using the helper in Chat entity
        User receiver = chat.getOtherUser(sender);

        // Build the message entity
        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .receiver(receiver)
                .content(inputDto.getContent())
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();

        // Save the message
        Message saved = messageRepository.save(message);
        notificationService.notifyNewMessage(sender, receiver, saved);
        // Map to DTO and return
        return messageMapper.toDto(saved);
    }


    // ------------------- GET MESSAGES BY CHAT -------------------
    @Override
    public List<MessageOutputDto> getMessagesByChatId(Long chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId).stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }
}
