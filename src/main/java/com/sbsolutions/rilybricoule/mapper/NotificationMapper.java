package com.sbsolutions.rilybricoule.mapper;

import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;

import com.sbsolutions.rilybricoule.dto.output.NotificationOutputDto;
import com.sbsolutions.rilybricoule.entity.Notification;
import com.sbsolutions.rilybricoule.entity.NotificationType;
import com.sbsolutions.rilybricoule.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationInputDto dto, User receiver) {
        if (dto == null) return null;

        return Notification.builder()
                .title("Notification")
                .contenu(dto.getContenu())
                .type(dto.getType() != null ? dto.getType() : NotificationType.MESSAGE)
                .date(dto.getDate() != null ? dto.getDate() : LocalDateTime.now())
                .vu(false)
                .channel("push")
                .triggeredBy("Système")
                .triggeredByRole("system")
                .receiver(receiver)
                .build();
    }

    // Map Entity -> Output DTO
    public NotificationOutputDto toDto(Notification notification) {
        if (notification == null) return null;

        NotificationOutputDto dto = new NotificationOutputDto();

        dto.setId(notification.getId());

        dto.setTitle(
                notification.getTitle() != null
                        ? notification.getTitle()
                        : "Notification"
        );

        dto.setMessage(notification.getContenu());
        dto.setContenu(notification.getContenu());

        dto.setType(notification.getType());

        dto.setDate(notification.getDate());
        dto.setSentAt(notification.getDate());

        dto.setVu(notification.isVu());
        dto.setRead(notification.isVu());

        dto.setChannel(
                notification.getChannel() != null
                        ? notification.getChannel()
                        : "push"
        );

        dto.setTriggeredBy(
                notification.getTriggeredBy() != null
                        ? notification.getTriggeredBy()
                        : "Système"
        );

        dto.setTriggeredByRole(
                notification.getTriggeredByRole() != null
                        ? notification.getTriggeredByRole()
                        : "system"
        );

        dto.setTriggeredById(notification.getTriggeredById());
        dto.setRedirectUrl(notification.getRedirectUrl());

        if (notification.getReceiver() != null) {
            dto.setReceiverId(notification.getReceiver().getId());
            dto.setReceiverName(
                    notification.getReceiver().getFirstName() + " " +
                            notification.getReceiver().getLastName()
            );
        }

        return dto;
    }

}
