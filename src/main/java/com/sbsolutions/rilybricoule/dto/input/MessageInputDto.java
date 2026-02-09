package com.sbsolutions.rilybricoule.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageInputDto {
    private String contenu;
    private Long chatId;  // to know which chat the message belongs to
    private Long auteurId; // usually the logged-in user, optional if backend handles it
}