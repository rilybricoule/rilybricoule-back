package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.input.MessageInputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.entity.Message;
import com.sbsolutions.rilybricoule.services.IMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Chat messages management")
public class MessageController {

    private final IMessageService messageService;

    // ------------------- SEND MESSAGE -------------------
    @Operation(summary = "Send a message in a chat",
            description = "Saves a message to a chat. The chat must exist and be active.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/send")
    public ResponseEntity<MessageOutputDto> sendMessage(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Message input DTO containing chat ID, sender, and content"
            )
            @RequestBody MessageInputDto dto
    ) {
        // Call the service to save the message
        Message message = messageService.sendMessage(
                dto.getChatId(),
                dto.getSender(),
                dto.getContenu()
        );

        // Convert entity to DTO for response
        MessageOutputDto output = MessageOutputDto.builder()
                .senderName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                .content(message.getContent())
                .imageUrl(message.getImageUrl()) // optional
                .sentAt(message.getSentAt())
                .read(message.isRead())
                .build();

        return ResponseEntity.ok(output);
    }

    // ------------------- GET MESSAGES BY CHAT -------------------
    @Operation(summary = "Get all messages in a chat",
            description = "Retrieves all messages for a chat, ordered by creation time ascending.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found")
    })
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<MessageOutputDto>> getMessagesByChat(
            @Parameter(description = "ID of the chat to retrieve messages for") @PathVariable Long chatId
    ) {
        List<MessageOutputDto> output = messageService.getMessagesByChatId(chatId).stream()
                .map(m -> MessageOutputDto.builder()
                        .senderName(m.getSender().getFirstName() + " " + m.getSender().getLastName())
                        .content(m.getContent())
                        .imageUrl(m.getImageUrl())
                        .sentAt(m.getSentAt())
                        .read(m.isRead())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(output);
    }
}
