package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.MessageInputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.entity.Chat;
import com.sbsolutions.rilybricoule.entity.Message;
import com.sbsolutions.rilybricoule.entity.MessageType;
import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.mapper.MessageMapper;
import com.sbsolutions.rilybricoule.repository.ChatRepository;
import com.sbsolutions.rilybricoule.repository.MessageRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final INotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final int RESTORE_DAYS = 7;
    private final BlockService blockService;

    @Override
    @Transactional
    public MessageOutputDto sendMessage(Long chatId, Long senderId, MessageInputDto inputDto) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.isActive()) {
            throw new IllegalStateException("Chat is closed");
        }

        // enforce path params into dto
        inputDto.setChatId(chatId);
        inputDto.setSenderId(senderId);

        return saveMessage(chatId, senderId, inputDto);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // 🔒 Security check — only sender can delete
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this message");
        }

        // If already deleted, do nothing
        if (message.isDeleted()) {
            return;
        }

        message.setDeleted(true);
        message.setDeletedAt(LocalDateTime.now());

        messageRepository.save(message);
    }

    @Override
    @Transactional
    public MessageOutputDto editMessage(Long messageId, Long userId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to edit this message");
        }

        if (message.isDeleted()) {
            throw new IllegalStateException("Cannot edit a deleted message");
        }

        message.setContent(newContent);
        message.setEditedAt(LocalDateTime.now());
        Message saved = messageRepository.save(message);

        MessageOutputDto dto = messageMapper.toDto(saved);
        Long chatId = saved.getChat().getId();
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, dto);

        return dto;




    }

    // ------------------- SAVE MESSAGE -------------------
    @Override
    @Transactional
    public MessageOutputDto saveMessage(Long chatId, Long senderId, MessageInputDto inputDto) {
        // Fetch chat
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found with id " + chatId));

        // Fetch sender
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found with id " + senderId));

        // Determine receiver
        User receiver = chat.getOtherUser(sender);

        // Resolve message type (backward compatible default)
        MessageType type = inputDto.getMessageType() != null ? inputDto.getMessageType() : MessageType.TEXT;

        // Normalize values
        String content = inputDto.getContent() != null ? inputDto.getContent().trim() : null;
        String mediaUrl = inputDto.getMediaUrl() != null ? inputDto.getMediaUrl().trim() : null;

        // Validate payload by type
        switch (type) {
            case TEXT -> {
                if (content == null || content.isEmpty()) {
                    throw new IllegalArgumentException("TEXT message requires non-empty content");
                }
            }
            case IMAGE, AUDIO, FILE -> {
                if (mediaUrl == null || mediaUrl.isEmpty()) {
                    throw new IllegalArgumentException(type + " message requires mediaUrl");
                }
            }
            case SYSTEM -> {
                // Optional rule: block user-created SYSTEM messages
                throw new IllegalArgumentException("SYSTEM messages cannot be sent from this endpoint");
            }
            default -> throw new IllegalArgumentException("Unsupported message type: " + type);
        }

        blockService.assertMessagingAllowed(sender.getId(), receiver.getId());


        // Build entity
        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .read(false)
                .messageType(type)
                // If your entity still uses imageUrl, map mediaUrl into it for now:
                .mediaUrl(mediaUrl)
                .build();

        // Save and notify
        Message saved = messageRepository.save(message);
        notificationService.notifyNewMessage(sender, receiver, saved);

        MessageOutputDto dto = messageMapper.toDto(saved);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, dto);

        return dto;
    }

    @Transactional
    public void markAsRead(Long chatId, Long receiverId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with id " + chatId));

        Long clientId = chat.getClient().getId();
        Long prestataireId = chat.getPrestataire().getId();

        if (!receiverId.equals(clientId) && !receiverId.equals(prestataireId)) {
            throw new IllegalArgumentException("Receiver does not belong to this chat");
        }

        messageRepository.markMessagesAsRead(chatId, receiverId);
    }


    @Override
    public long getUnreadMessageCount(Long userId) {
        return messageRepository.countUnreadByReceiverId(userId);
    }


    @Override
    public long getUnreadMessageCountForChat(Long chatId, Long userId) {
        return messageRepository.countUnreadByChatIdAndReceiverId(chatId, userId);
    }

    // ------------------- GET MESSAGES BY CHAT -------------------
    @Override
    public List<MessageOutputDto> getMessagesByChatId(Long chatId) {
        return messageRepository.findActiveByChatIdOrderByCreatedAtAsc(chatId).stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public int purgeDeletedMessagesOlderThanSevenDays() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RESTORE_DAYS);
        List<Message> toPurge = messageRepository.findByDeletedTrueAndDeletedAtBefore(threshold);
        int count = toPurge.size();
        if (count > 0) {
            messageRepository.deleteAll(toPurge);
            log.info("Permanently deleted {} messages (soft-deleted more than {} days ago)", count, RESTORE_DAYS);
        }
        return count;
    }


    @Override
    @Transactional
    public void restoreConversation(Long chatId, Long userId) {
        // 1) Chat exists and user is participant
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        if (!chat.getClient().getId().equals(userId) && !chat.getPrestataire().getId().equals(userId)) {
            throw new RuntimeException("You are not part of this chat");
        }

        // 2) Only messages deleted within the last 7 days
        LocalDateTime after = LocalDateTime.now().minusDays(RESTORE_DAYS);
        List<Message> restorable = messageRepository.findByChatIdAndDeletedTrueAndDeletedAtAfter(chatId, after);

        // 3) Restore each
        for (Message m : restorable) {
            m.setDeleted(false);
            m.setDeletedAt(null);
            messageRepository.save(m);
        }





    }



}
