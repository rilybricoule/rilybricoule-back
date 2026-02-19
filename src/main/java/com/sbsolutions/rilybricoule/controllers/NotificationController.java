package com.sbsolutions.rilybricoule.controllers;



import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.dto.output.NotificationOutputDto;
import com.sbsolutions.rilybricoule.entity.Notification;
import com.sbsolutions.rilybricoule.services.INotificationService;
import com.sbsolutions.rilybricoule.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "APIs to manage notifications for users")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody NotificationInputDto dto) {
        try {
            Notification saved = notificationService.createNotification(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // B) lister notifications d’un prestataire (pour vérifier facilement)
    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<NotificationOutputDto>> getByPrestataire(@PathVariable Long prestataireId) {
        List<Notification> list = notificationService.getPrestataireNotifications(prestataireId);
        return ResponseEntity.ok(
                list.stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    // Mapping -> Output DTO
    private NotificationOutputDto toDto(Notification n) {
        return NotificationOutputDto.builder()
                .id(n.getId())
                .contenu(n.getContenu())
                .date(n.getDate())
                .type(n.getType())
                .prestataireId(n.getPrestataire() != null ? n.getPrestataire().getId() : null)
                .build();
    }

    private final INotificationService notificationService;




    // ✅ Get notifications for a user
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications for a user",
            description = "Returns a list of notifications for the given user ID")
    public ResponseEntity<List<NotificationOutputDto>> getNotificationsForUser(
            @PathVariable Long userId) {

        List<NotificationOutputDto> notifications =
                notificationService.getNotificationsForUser(userId);

        return ResponseEntity.ok(notifications);
    }

    // ✅ Mark as read
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read",
            description = "Marks a notification as read by its ID")
    public ResponseEntity<NotificationOutputDto> markAsRead(
            @PathVariable Long notificationId) {

        NotificationOutputDto updated =
                notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(updated);
    }

    // ✅ Delete all notifications for user
    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Delete all notifications for a user",
            description = "Deletes all notifications for the given user ID")
    public ResponseEntity<Void> deleteAllNotificationsForUser(
            @PathVariable Long userId) {

        notificationService.deleteAllNotificationsForUser(userId);
        return ResponseEntity.noContent().build();
    }
}
