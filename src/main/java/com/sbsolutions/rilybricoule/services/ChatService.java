package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.ChatInputDto;
import com.sbsolutions.rilybricoule.dto.output.ChatOutputDto;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    public Chat startOrGetChat(
            Client client,
            Prestataire prestataire,
            Reservation reservation
    ) {
        return chatRepository
                .findByClientAndPrestataireAndReservation(
                        client, prestataire, reservation
                )
                .orElseGet(() -> {
                    Chat chat = Chat.builder()
                            .client(client)
                            .prestataire(prestataire)
                            .reservation(reservation)
                            .createdAt(LocalDateTime.now())
                            .active(true)
                            .build();
                    return chatRepository.save(chat);
                });
    }
}
