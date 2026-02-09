package com.sbsolutions.rilybricoule.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatInputDto {
    private Long clientId;
    private Long prestataireId;
    private Long reservationId;

}
