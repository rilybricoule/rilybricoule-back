package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.dto.output.NotificationOutputDto;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.entity.Message;
import com.sbsolutions.rilybricoule.entity.Reservation;
import com.sbsolutions.rilybricoule.entity.User;

import java.util.List;

public interface INotificationService {

    // Notify a new message and return the notification DTO
    NotificationOutputDto notifyNewMessage(User sender, User receiver, Message message);

    // Notify a reservation and return the notification DTO
    NotificationOutputDto notifyReservation(Client client, Reservation reservation);

    // Create a notification from input DTO
    NotificationOutputDto createNotification(NotificationInputDto inputDto);

    // Get all notifications for a user
    List<NotificationOutputDto> getNotificationsForUser(Long userId);

    // Mark a notification as read
    NotificationOutputDto markAsRead(Long notificationId);

    // Delete all notifications for a user
    void deleteAllNotificationsForUser(Long userId);
}
