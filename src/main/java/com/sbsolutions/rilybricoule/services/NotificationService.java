package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.dto.output.NotificationOutputDto;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.mapper.NotificationMapper;
import com.sbsolutions.rilybricoule.repository.NotificationRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final JavaMailSender mailSender;

    @Value("${app.notifications.webhook.enabled:false}")
    private boolean webhookEnabled;

    @Value("${app.notifications.webhook.url:}")
    private String webhookUrl;

    @Value("${app.notifications.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.notifications.email.from:}")
    private String emailFrom;


    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;


    // Notify a new message
    @Override
    public NotificationOutputDto notifyNewMessage(User sender, User receiver, Message message) {
        String preview = message.getContent().length() > 30
                ? message.getContent().substring(0, 30) + "..."
                : message.getContent();

        NotificationInputDto inputDto = new NotificationInputDto();
        inputDto.setContenu(sender.getFirstName() + " " + sender.getLastName() + " sent you a message: " + preview);
        inputDto.setType(NotificationType.MESSAGE);
        inputDto.setReceiverId(receiver.getId());

        return createNotification(inputDto);
    }


    private void pushNotificationToUser(Long userId, NotificationOutputDto dto) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, dto);
    }


    @Override
    public NotificationOutputDto notifyReservation(Client client, Reservation reservation) {
        // Create DTO instance
        NotificationInputDto inputDto = new NotificationInputDto();
        inputDto.setContenu("New reservation from " + client.getFirstName() + " " + client.getLastName()
                + " for reservation ID: " + reservation.getId());
        inputDto.setType(NotificationType.RESERVATION);
        inputDto.setReceiverId(reservation.getPrestataire().getId());
        inputDto.setDate(null);

        // Convert DTO -> entity and save
        Notification notification = notificationMapper.toEntity(inputDto, reservation.getPrestataire());
        Notification saved = notificationRepository.save(notification);

        NotificationOutputDto dto = notificationMapper.toDto(saved);
        pushNotificationToUser(reservation.getPrestataire().getId(), dto);
        sendWebhook("RESERVATION", saved, reservation, client);
        if (reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
              sendEmail(
                      reservation.getPrestataire().getEmail(),
                      "Réservation confirmée",
                      inputDto.getContenu()
              );
        }
        return dto;
    }

    // Create a notification from input DTO
    @Override
    public NotificationOutputDto createNotification(NotificationInputDto inputDto) {
        // Use the inputDto instance, not the class
        User receiver = userRepository.findById(inputDto.getReceiverId())
                .orElseThrow(() -> new RuntimeException(
                        "Receiver not found with id " + inputDto.getReceiverId()
                ));

        // Map DTO -> entity
        Notification notification = notificationMapper.toEntity(inputDto, receiver);

        // Save entity
        Notification saved = notificationRepository.save(notification);

        // Map entity -> output DTO

        NotificationOutputDto dto = notificationMapper.toDto(saved);
        pushNotificationToUser(receiver.getId(), dto);
        return dto;

    }

    private void sendWebhook(String event, Notification notification, Reservation reservation, Client client) {
        if (!webhookEnabled) return;
        if (webhookUrl == null || webhookUrl.isBlank()) return;

        try {
            Map<String, Object> payload = Map.of(
                    "event", event,
                    "notificationId", notification.getId(),
                    "content", notification.getContenu(),
                    "date", notification.getDate() !=null ? notification.getDate().toString() : null,
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

    // Get all notifications for a user
    @Override
    public List<NotificationOutputDto> getNotificationsForUser(Long userId) {
        return notificationRepository.findByReceiver_IdOrderByDateDesc(userId).stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    // Mark notification as read
    @Override
    public NotificationOutputDto markAsRead(Long notificationId) {
        Notification updated = notificationRepository.findById(notificationId)
                .map(notification -> {
                    notification.setVu(true);
                    return notificationRepository.save(notification);
                })
                .orElseThrow(() -> new RuntimeException("Notification not found with id " + notificationId));

        return notificationMapper.toDto(updated);
    }


    @Override
    public void deleteAllNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository.findByReceiver_IdOrderByDateDesc(userId);
        if (!notifications.isEmpty()) {
            notificationRepository.deleteAll(notifications);
        }
    }

}
