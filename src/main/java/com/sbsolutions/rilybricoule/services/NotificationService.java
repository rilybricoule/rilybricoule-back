package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.dto.PrestaireDTO;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    // ----------------------------
    // Implémentation de INotificationService
    // ----------------------------

    @Override
    public void notifyNewMessage(Client sender, Prestataire receiver, Message message) {
        String content = sender.getFirstName() + " " + sender.getLastName() +
                " sent a message: " + message.getContent();

        Notification notification = Notification.builder()
                .contenu(content)
                .date(LocalDateTime.now())
                .type(NotificationType.MESSAGE)
                .prestataire(receiver)   // stocke l'entité Prestataire
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public void notifyReservation(Client client, Reservation reservation) {
        String content = "New reservation from " + client.getFirstName() + " " + client.getLastName() +
                " for reservation ID: " + reservation.getId();

        Notification notification = Notification.builder()
                .contenu(content)
                .date(LocalDateTime.now())
                .type(NotificationType.RESERVATION)
                .prestataire(reservation.getPrestataire())
                .build();

        notificationRepository.save(notification);
    }

    // ----------------------------
    // Méthodes pour controller avec DTO
    // ----------------------------

    public Notification createNotification(NotificationInputDto dto) {
        // Ici on peut soit récupérer Prestataire via ID, soit juste stocker ID dans l'entité
        Notification notification = Notification.builder()
                .contenu(dto.getContenu())
                .date(dto.getDate() != null ? dto.getDate() : LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }




}
