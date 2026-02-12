package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.dto.output.NotificationOutputDto;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.mapper.NotificationMapper;
import com.sbsolutions.rilybricoule.repository.NotificationRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    // Notify a new message
    @Override
    public NotificationOutputDto notifyNewMessage(User sender, User receiver, Message message) {
        String preview = message.getContent().length() > 30
                ? message.getContent().substring(0, 30) + "..."
                : message.getContent();

        NotificationInputDto inputDto = new NotificationInputDto();
        inputDto.setContenu(sender.getFirstName() + " " + sender.getLastName() + " sent you a message: " + preview);
        inputDto.setType(NotificationType.MESSAGE);
        inputDto.setPrestataireId(receiver.getId());

        return createNotification(inputDto);
    }


    @Override
    public NotificationOutputDto notifyReservation(Client client, Reservation reservation) {
        // Create DTO instance
        NotificationInputDto inputDto = new NotificationInputDto();
        inputDto.setContenu("New reservation from " + client.getFirstName() + " " + client.getLastName()
                + " for reservation ID: " + reservation.getId());
        inputDto.setType(NotificationType.RESERVATION);
        inputDto.setPrestataireId(reservation.getPrestataire().getId());
        inputDto.setDate(null);

        // Convert DTO -> entity and save
        Notification notification = notificationMapper.toEntity(inputDto, reservation.getPrestataire());
        Notification saved = notificationRepository.save(notification);

        // Return DTO for frontend
        return notificationMapper.toDto(saved);
    }


    // Create a notification from input DTO
    @Override
    public NotificationOutputDto createNotification(NotificationInputDto inputDto) {
        // Use the inputDto instance, not the class
        User receiver = userRepository.findById(inputDto.getPrestataireId())
                .orElseThrow(() -> new RuntimeException(
                        "Receiver not found with id " + inputDto.getPrestataireId()
                ));

        // Map DTO -> entity
        Notification notification = notificationMapper.toEntity(inputDto, receiver);

        // Save entity
        Notification saved = notificationRepository.save(notification);

        // Map entity -> output DTO
        return notificationMapper.toDto(saved);
    }

    // Get all notifications for a user
    @Override
    public List<NotificationOutputDto> getNotificationsForUser(Long userId) {
        return notificationRepository.findByReceiverIdOrderByDateDesc(userId).stream()
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
        List<Notification> notifications = notificationRepository.findByReceiverIdOrderByDateDesc(userId);
        if (!notifications.isEmpty()) {
            notificationRepository.deleteAll(notifications);
        }
    }

}