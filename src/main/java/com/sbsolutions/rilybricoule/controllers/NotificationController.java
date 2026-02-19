package com.sbsolutions.rilybricoule.controllers;



import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.dto.output.NotificationOutputDto;
import com.sbsolutions.rilybricoule.entity.Notification;
import com.sbsolutions.rilybricoule.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
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




}
