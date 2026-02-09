package com.sbsolutions.rilybricoule.dto.output;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageOutputDto {

    private String senderName;    // nom de l'auteur
    private String content;       // texte du message
    private String imageUrl;      // URL de l'image (optionnel)
    private LocalDateTime sentAt; // date d'envoi
    private boolean read;         // lu ou non
}
