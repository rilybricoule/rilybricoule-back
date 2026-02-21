package com.sbsolutions.rilybricoule.services;

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
}
