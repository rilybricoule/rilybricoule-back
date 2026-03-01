package com.sbsolutions.rilybricoule.dto.output;

import com.sbsolutions.rilybricoule.entity.MessageType;
import com.sbsolutions.rilybricoule.entity.User;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageOutputDto {

    private Long id;


    private Long senderId;

    private Long receiverId;

    private String content;

    private String senderName;

    private LocalDateTime createdAt;

    private MessageType messageType;

    private String mediaUrl;



    private String receiverName;

    private boolean read;


    private LocalDateTime readAt;
}