package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.dto.output.NotificationOutputDto;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.mapper.NotificationMapper;
import com.sbsolutions.rilybricoule.repository.NotificationRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
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
        if (reservation.getPrestataire() == null) {
            return null;
        }
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
        return dto;
    }

    @Override
    public NotificationOutputDto notifyDispatchToPrestataire(Prestataire prestataire, Reservation reservation) {
        NotificationInputDto inputDto = new NotificationInputDto();
        inputDto.setContenu("New dispatch request for reservation ID: " + reservation.getId());
        inputDto.setType(NotificationType.RESERVATION);
        inputDto.setReceiverId(prestataire.getId());
        inputDto.setDate(null);

        Notification notification = notificationMapper.toEntity(inputDto, prestataire);
        Notification saved = notificationRepository.save(notification);

        NotificationOutputDto dto = notificationMapper.toDto(saved);
        pushNotificationToUser(prestataire.getId(), dto);
        return dto;
    }

    @Override
    public NotificationOutputDto notifyDispatchAccepted(Prestataire prestataire, Reservation reservation) {
        NotificationInputDto inputDto = new NotificationInputDto();
        inputDto.setContenu("You have accepted reservation ID: " + reservation.getId());
        inputDto.setType(NotificationType.RESERVATION);
        inputDto.setReceiverId(prestataire.getId());
        inputDto.setDate(null);

        Notification notification = notificationMapper.toEntity(inputDto, prestataire);
        Notification saved = notificationRepository.save(notification);

        NotificationOutputDto dto = notificationMapper.toDto(saved);
        pushNotificationToUser(prestataire.getId(), dto);
        return dto;
    }

    @Override
    public NotificationOutputDto notifyDispatchFailedToClient(Client client, Reservation reservation) {
        NotificationInputDto inputDto = new NotificationInputDto();
        inputDto.setContenu(
                "Dispatch failed for reservation ID: " + reservation.getId()
                        + ". No prestataire accepted your request. You can retry later or create a manual reservation."
        );
        inputDto.setType(NotificationType.RESERVATION);
        inputDto.setReceiverId(client.getId());
        inputDto.setDate(null);

        Notification notification = notificationMapper.toEntity(inputDto, client);
        Notification saved = notificationRepository.save(notification);

        NotificationOutputDto dto = notificationMapper.toDto(saved);
        pushNotificationToUser(client.getId(), dto);
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