package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.ChatInputDto;
import com.sbsolutions.rilybricoule.dto.output.ChatOutputDto;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.repository.ChatRepository;
import com.sbsolutions.rilybricoule.repository.MessageRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ChatService implements IChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    public Chat startOrGetChat(Long senderId, Long receiverId, Long reservationId) {
        return chatRepository
                .findByClientIdAndPrestataireIdAndReservationId(senderId, receiverId, reservationId)
                .orElseGet(() -> {
                    User client = userRepository.findById(senderId)
                            .orElseThrow(() -> new RuntimeException("Client not found"));
                    User prestataire = userRepository.findById(receiverId)
                            .orElseThrow(() -> new RuntimeException("Prestataire not found"));
                    Reservation reservation = null;
                    if (reservationId != null) {
                        reservation = reservationRepository.findById(reservationId)
                                .orElseThrow(() -> new RuntimeException("Reservation not found"));
                    }

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
