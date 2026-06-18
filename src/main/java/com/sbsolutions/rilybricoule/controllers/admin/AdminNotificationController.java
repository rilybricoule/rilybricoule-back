package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.output.NotificationOutputDto;
import com.sbsolutions.rilybricoule.services.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final INotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationOutputDto> getAll() {
        return notificationService.getAllNotifications();
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyAuthority('NOTIFICATIONS_SEND_TARGETED', 'NOTIFICATIONS_SEND_ALL')")
    public NotificationOutputDto markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyAuthority('NOTIFICATIONS_SEND_TARGETED', 'NOTIFICATIONS_SEND_ALL')")
    public void markAllAsRead() {
        notificationService.markAllAsRead();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('NOTIFICATIONS_SEND_TARGETED', 'NOTIFICATIONS_SEND_ALL')")
    public void delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }
}