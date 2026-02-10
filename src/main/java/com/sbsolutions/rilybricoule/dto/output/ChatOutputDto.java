package com.sbsolutions.rilybricoule.dto.output;

import com.sbsolutions.rilybricoule.entity.Reservation;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatOutputDto {



    private String clientName;
    private String prestataireName;
    private LocalDateTime createdAt;     // date de création du chat

    private LocalDateTime lastMessageAt; // date du dernier message
    private boolean active;              // chat actif ou non
    private List<MessageOutputDto> messages; // liste des messages
}


