package com.sbsolutions.rilybricoule.services;
import java.util.Collections;
import java.util.List;

import com.sbsolutions.rilybricoule.dto.output.ChatOutputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.repository.ChatRepository;
import com.sbsolutions.rilybricoule.repository.ClientRepository;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService implements IChatService {

    private final ChatRepository chatRepository;
    private final ClientRepository clientRepository;
    private final PrestaireRepository prestataireRepository;
    private final ReservationRepository reservationRepository;

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

        return chats.stream().map(chat -> {
            List<MessageOutputDto> messageDtos = Optional.ofNullable(chat.getMessages())
                    .orElse(Collections.emptyList())
                    .stream()
                    .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                    .map(msg -> MessageOutputDto.builder()
                            .id(msg.getId())
                            .senderName(msg.getSender().getFirstName())
                            .content(msg.getContent())
                            .createdAt(msg.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());

            return ChatOutputDto.builder()
                    .chatId(chat.getId())
                    .clientId(chat.getClient().getId())
                    .prestataireId(chat.getPrestataire().getId())
                    .clientFirstName(chat.getClient().getFirstName())
                    .clientLastName(chat.getClient().getLastName())
                    .prestataireFirstName(chat.getPrestataire().getFirstName())
                    .prestataireLastName(chat.getPrestataire().getLastName())
                    .reservationId(chat.getReservation() != null ? chat.getReservation().getId() : null)
                    .createdAt(chat.getCreatedAt())
                    .active(chat.isActive())
                    .lastMessageAt(chat.getLastMessageAt())
                    .messages(messageDtos)
                    .build();
        }).collect(Collectors.toList());
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
