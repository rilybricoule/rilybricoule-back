package com.sbsolutions.rilybricoule.mapper;

import com.sbsolutions.rilybricoule.dto.input.NotificationInputDto;
import com.sbsolutions.rilybricoule.dto.output.NotificationOutputDto;
import com.sbsolutions.rilybricoule.entity.notification.Notification;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationInputDto dto, Prestataire prestataire) {
        return Notification.builder()
                .contenu(dto.getContenu())
                .type(dto.getType())
                .prestataire(prestataire)
                .date(LocalDateTime.now())
                .vu(false)
                .build();
    }

    public NotificationOutputDto toOutputDto(Notification notification) {
        return new NotificationOutputDto(
                notification.getContenu(),
                notification.getDate()

        );
    }
}
