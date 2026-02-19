package com.sbsolutions.rilybricoule.dto.input;



import com.sbsolutions.rilybricoule.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageInputDto {

    private Long chatId;

    private String content;

    private Long senderId;

    private Long receiverId;

    private String imageUrl;


    private LocalDateTime sentAt;

    private MessageType messageType;// optional
}
