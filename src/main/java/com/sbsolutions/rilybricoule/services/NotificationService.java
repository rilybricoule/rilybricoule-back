package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.dto.PrestaireDTO;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.repository.NotificationRepository;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final PrestaireRepository prestaireRepository;
    private final JavaMailSender mailSender;

    @Value("${app.notifications.webhook.enabled:false}")
    private boolean webhookEnabled;

    @Value("${app.notifications.webhook.url:}")
    private String webhookUrl;

    @Value("${app.notifications.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.notifications.email.from:}")
    private String emailFrom;

    public List<Notification> getPrestataireNotifications(Long prestataireId) {
        return notificationRepository.findByPrestataire_IdOrderByDateDesc(prestataireId);
    }


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
        String content =
                "Nouvelle réservation (" + reservation.getStatus() + ")\n" +
                        "Client: " + client.getFirstName() + " " + client.getLastName() + "\n" +
                        "Date: " + reservation.getReservationDate() + " " + reservation.getReservationTime() + "\n" +
                        "Total: " + reservation.getTotalPrice() + "\n" +
                        "Reservation ID: " + reservation.getId();

        Notification notification = Notification.builder()
                .contenu(content)
                .date(LocalDateTime.now())
                .type(NotificationType.RESERVATION)
                .prestataire(reservation.getPrestataire())
                .build();

        notificationRepository.save(notification);

        sendWebhook("RESERVATION", notification, reservation, client);
        if (reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
            sendEmail(
                    reservation.getPrestataire().getEmail(),
                    "Réservation confirmée",
                    content
            );
        }
    }

    // ----------------------------
    // Méthodes pour controller avec DTO
    // ----------------------------

    public Notification createNotification(NotificationInputDto dto) {
        // Ici on peut soit récupérer Prestataire via ID, soit juste stocker ID dans l'entité
        if (dto.getPrestataireId() == null) {
            throw new IllegalArgumentException("prestataireId is required");
        }

        Prestataire prestataire = prestaireRepository.findById(dto.getPrestataireId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Prestataire not found with id: " + dto.getPrestataireId()
                ));

        Notification notification = Notification.builder()
                .contenu(dto.getContenu())
                .date(dto.getDate() != null ? dto.getDate() : LocalDateTime.now())
                .type(dto.getType() != null ? dto.getType() : NotificationType.MESSAGE)
                .prestataire(prestataire)
                .build();

        return notificationRepository.save(notification);
    }

    private void sendWebhook(String event, Notification notification, Reservation reservation, Client client) {
        if (!webhookEnabled) return;
        if (webhookUrl == null || webhookUrl.isBlank()) return;

        try {
            Map<String, Object> payload = Map.of(
                    "event", event,
                    "notificationId", notification.getId(),
                    "content", notification.getContenu(),
                    "date", notification.getDate().toString(),
                    "reservationId", reservation.getId(),
                    "clientId", client.getId(),
                    "prestataireId", reservation.getPrestataire().getId()
            );
            restTemplate.postForEntity(webhookUrl, payload, String.class);
        } catch (Exception e) {}


    }
    private void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) return;
        if (to == null || to.isBlank()) return;

        if (emailFrom == null || emailFrom.isBlank()) {
            throw new IllegalArgumentException("app.notifications.email.from is missing");
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setFrom(emailFrom);
            msg.setSubject(subject);
            msg.setText(body);

            mailSender.send(msg);
        } catch (Exception e) {e.printStackTrace();}
    }




}
