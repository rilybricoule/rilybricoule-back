package com.sbsolutions.rilybricoule.dto.output;

import com.sbsolutions.rilybricoule.entity.NotificationType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationOutputDto {



    private Long id;
    private String contenu;          // texte de la notification
    private LocalDateTime date;      // date de création
    private NotificationType type;
    private  boolean vu ;
    private  Long receiverId;
    private String receiverName;

}
