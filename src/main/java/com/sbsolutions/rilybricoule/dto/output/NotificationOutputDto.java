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

    private String title;
    private String message;

    private String contenu;
    private LocalDateTime date;

    private NotificationType type;

    private boolean vu;
    private boolean read;

    private String channel;

    private String triggeredBy;
    private String triggeredByRole;
    private Long triggeredById;

    private LocalDateTime sentAt;

    private Long receiverId;
    private String receiverName;

    private String redirectUrl;
}