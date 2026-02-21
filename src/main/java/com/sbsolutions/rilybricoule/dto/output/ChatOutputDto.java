package com.sbsolutions.rilybricoule.dto.output;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatOutputDto {

    private Long chatId;
    private Long clientId;
    private Long prestataireId;
    private String clientFirstName;
    private String clientLastName;
    private String prestataireFirstName;
    private String prestataireLastName;
    private Long reservationId;
    private LocalDateTime createdAt;
    private Boolean active;
    private LocalDateTime lastMessageAt;
    private List<MessageOutputDto> messages;
}