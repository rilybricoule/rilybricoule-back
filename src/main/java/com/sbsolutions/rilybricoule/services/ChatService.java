package com.sbsolutions.rilybricoule.services;
import java.util.Collections;
import java.util.List;

import com.sbsolutions.rilybricoule.dto.output.ChatOutputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class
ChatService implements IChatService {


    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ClientRepository clientRepository;
    private final PrestaireRepository prestataireRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ChatOutputDto openGenericChat(Long userOneId, Long userTwoId, ChatType type) {
        if (userOneId.equals(userTwoId)) {
            throw new IllegalArgumentException("You cannot open a chat with yourself");
        }

        User userOne = userRepository.findById(userOneId)
                .orElseThrow(() -> new RuntimeException("First user not found"));

        User userTwo = userRepository.findById(userTwoId)
                .orElseThrow(() -> new RuntimeException("Second user not found"));

        Chat chat = chatRepository.findGenericChat(userOneId, userTwoId)
                .orElseGet(() -> chatRepository.save(
                        Chat.builder()
                                .participantOne(userOne)
                                .participantTwo(userTwo)
                                .type(type)
                                .createdAt(LocalDateTime.now())
                                .active(true)
                                .messages(new ArrayList<>())
                                .build()
                ));

        return mapToDto(chat, userOneId);
    }


    private ChatOutputDto mapToDto(Chat chat, Long currentUserId) {
        List<MessageOutputDto> messageDtos = Optional.ofNullable(chat.getMessages())
                .orElse(Collections.emptyList())
                .stream()
                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                .map(msg -> MessageOutputDto.builder()
                        .id(msg.getId())
                        .senderId(msg.getSender().getId())
                        .receiverId(msg.getReceiver().getId())
                        .senderName(msg.getSender().getFirstName() + " " + msg.getSender().getLastName())
                        .receiverName(msg.getReceiver().getFirstName() + " " + msg.getReceiver().getLastName())
                        .content(msg.getContent())
                        .createdAt(msg.getCreatedAt())
                        .messageType(msg.getMessageType())
                        .mediaUrl(msg.getMediaUrl())
                        .read(msg.isRead())
                        .readAt(msg.getReadAt())
                        .build())
                .toList();

        User otherUser = chat.getOtherUser(
                userRepository.findById(currentUserId)
                        .orElseThrow(() -> new RuntimeException("Current user not found"))
        );

        MessageOutputDto lastMessage = messageDtos.isEmpty()
                ? null
                : messageDtos.get(messageDtos.size() - 1);

        long unreadCount = messageDtos.stream()
                .filter(message -> message.getReceiverId() != null)
                .filter(message -> message.getReceiverId().equals(currentUserId))
                .filter(message -> !message.isRead())
                .count();

        return ChatOutputDto.builder()
                .chatId(chat.getId())
                .clientId(chat.getClient() != null ? chat.getClient().getId() : null)
                .prestataireId(chat.getPrestataire() != null ? chat.getPrestataire().getId() : null)
                .clientFirstName(chat.getClient() != null ? chat.getClient().getFirstName() : null)
                .clientLastName(chat.getClient() != null ? chat.getClient().getLastName() : null)
                .prestataireFirstName(chat.getPrestataire() != null ? chat.getPrestataire().getFirstName() : null)
                .prestataireLastName(chat.getPrestataire() != null ? chat.getPrestataire().getLastName() : null)
                .reservationId(chat.getReservation() != null ? chat.getReservation().getId() : null)
                .createdAt(chat.getCreatedAt())
                .active(chat.isActive())
                .lastMessageAt(chat.getLastMessageAt())
                .messages(messageDtos)

                // these fields must exist in ChatOutputDto
                .displayName(otherUser.getFirstName() + " " + otherUser.getLastName())
                .avatarLetter(otherUser.getFirstName() != null && !otherUser.getFirstName().isBlank()
                        ? otherUser.getFirstName().substring(0, 1).toUpperCase()
                        : "U")
                .lastMessagePreview(lastMessage != null ? lastMessage.getContent() : "")
                .unreadCount(unreadCount)
                .build();
    }


    @Transactional
    public Chat startOrGetChat(Long clientId, Long prestataireId, Long reservationId) {
        Chat chat = chatRepository
                .findByClientIdAndPrestataireIdAndReservationId(clientId, prestataireId, reservationId)
                .orElseGet(() -> {
                    Client client = clientRepository.findById(clientId)
                            .orElseThrow(() -> new RuntimeException("Client not found"));
                    Prestataire prestataire = prestataireRepository.findById(prestataireId)
                            .orElseThrow(() -> new RuntimeException("Prestataire not found"));
                    Reservation reservation = null;
                    if (reservationId != null) {
                        reservation = reservationRepository.findById(reservationId)
                                .orElseThrow(() -> new RuntimeException("Reservation not found"));
                    }

                    Chat newChat = Chat.builder()
                            .client(client)
                            .prestataire(prestataire)
                            .reservation(reservation)
                            .createdAt(LocalDateTime.now())
                            .active(true)
                            .messages(new ArrayList<>())
                            .build();
                    return chatRepository.save(newChat);
                });

        // Load lazy associations before returning (avoids LazyInitializationException in controller)
        chat.getClient().getFirstName();
        chat.getPrestataire().getFirstName();
        if (chat.getMessages() != null) {
            chat.getMessages().forEach(m -> m.getSender().getFirstName());
        }

        return chat;
    }
    @Transactional
    public void disableConversation(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        if (!chat.getClient().getId().equals(userId) && !chat.getPrestataire().getId().equals(userId)) {
            throw new RuntimeException("You are not part of this chat");
        }
        chat.setActive(false);
        chatRepository.save(chat);
    }

    @Transactional
    public void enableConversation(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        if (!chat.getClient().getId().equals(userId) && !chat.getPrestataire().getId().equals(userId)) {
            throw new RuntimeException("You are not part of this chat");
        }
        chat.setActive(true);
        chatRepository.save(chat);
    }




    @Override
    @Transactional
    public void pinConversation(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (chat.getClient().getId().equals(userId)) {
            chat.setPinnedByClientAt(LocalDateTime.now());
        } else if (chat.getPrestataire().getId().equals(userId)) {
            chat.setPinnedByPrestataireAt(LocalDateTime.now());
        } else {
            throw new RuntimeException("You are not part of this chat");
        }

        chatRepository.save(chat);
    }

    @Override
    @Transactional
    public void restoreDeletedConversation(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        LocalDateTime threshold = LocalDateTime.now().minusDays(7);

        if (chat.getClient().getId().equals(userId)) {
            LocalDateTime deletedAt = chat.getDeletedByClientAt();
            if (deletedAt == null) return;
            if (deletedAt.isBefore(threshold)) {
                throw new IllegalStateException("Restore window expired (7 days)");
            }
            chat.setDeletedByClientAt(null);

        } else if (chat.getPrestataire().getId().equals(userId)) {
            LocalDateTime deletedAt = chat.getDeletedByPrestataireAt();
            if (deletedAt == null) return;
            if (deletedAt.isBefore(threshold)) {
                throw new IllegalStateException("Restore window expired (7 days)");
            }
            chat.setDeletedByPrestataireAt(null);

        } else {
            throw new RuntimeException("You are not part of this chat");
        }

        chatRepository.save(chat);
    }




    @Override
    @Transactional
    public void unpinConversation(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (chat.getClient().getId().equals(userId)) {
            chat.setPinnedByClientAt(null);
        } else if (chat.getPrestataire().getId().equals(userId)) {
            chat.setPinnedByPrestataireAt(null);
        } else {
            throw new RuntimeException("You are not part of this chat");
        }

        chatRepository.save(chat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatOutputDto> getChatsByUserId(Long userId) {
        List<Chat> chats = chatRepository.findActivechatsByUserId(userId);

        return chats.stream()
                .map(chat -> mapToDto(chat, userId))
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void unarchiveConversation(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (chat.getClient().getId().equals(userId)) {
            chat.setArchivedByClientAt(null);
        } else if (chat.getPrestataire().getId().equals(userId)) {
            chat.setArchivedByPrestataireAt(null);
        } else {
            throw new RuntimeException("You are not part of this chat");
        }

        chatRepository.save(chat);
    }
    @Override
    @Transactional
    public void deleteConversation(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (chat.getClient().getId().equals(userId)) {
            chat.setDeletedByClientAt(LocalDateTime.now());
        } else if (chat.getPrestataire().getId().equals(userId)) {
            chat.setDeletedByPrestataireAt(LocalDateTime.now());
        } else {
            throw new RuntimeException("You are not part of this chat");
        }

        // Optional cleanup: hard delete only if both users deleted
        if (chat.isDeletedForBothUsers()) {
            chatRepository.delete(chat);
            return;
        }

        chatRepository.save(chat);
    }

    @Override
    @Transactional
    public void archiveConversation(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (chat.getClient().getId().equals(userId)) {
            chat.setArchivedByClientAt(LocalDateTime.now());
        } else if (chat.getPrestataire().getId().equals(userId)) {
            chat.setArchivedByPrestataireAt(LocalDateTime.now());
        } else {
            throw new RuntimeException("You are not part of this chat");
        }

        chatRepository.save(chat);
    }


}
