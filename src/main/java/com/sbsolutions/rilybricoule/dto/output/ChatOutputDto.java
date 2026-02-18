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

    private Long chatId;
    private String clientName;
    private String prestataireName;
    private LocalDateTime createdAt;
}


