package com.sbsolutions.rilybricoule.mapper;

import com.sbsolutions.rilybricoule.dto.input.MessageInputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.entity.Message;
import com.sbsolutions.rilybricoule.entity.MessageType;
import com.sbsolutions.rilybricoule.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public Message toEntity(MessageInputDto dto, User sender, User receiver) {
        if (dto == null) return null;

        MessageType type = dto.getMessageType() != null ? dto.getMessageType() : MessageType.TEXT;

        return Message.builder()
                .content(dto.getContent())
                .sender(sender)
                .receiver(receiver)
                .messageType(type)
                .mediaUrl(dto.getMediaUrl())
                .read(false)
                .build();
    }

    public MessageOutputDto toDto(Message message) {
        if (message == null) return null;

        return MessageOutputDto.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getFirstName() + " " + message.getReceiver().getLastName())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .mediaUrl(message.getMediaUrl())
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .build();
    }
}