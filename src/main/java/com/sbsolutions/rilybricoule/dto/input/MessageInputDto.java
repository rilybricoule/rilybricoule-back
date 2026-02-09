package com.sbsolutions.rilybricoule.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageInputDto {
    private String contenu;
    private Long chatId;  // ID du chat
}