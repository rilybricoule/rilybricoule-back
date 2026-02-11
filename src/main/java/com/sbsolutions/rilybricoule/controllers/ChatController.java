package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.input.ChatInputDto;
import com.sbsolutions.rilybricoule.dto.input.MessageInputDto;
import com.sbsolutions.rilybricoule.dto.output.ChatOutputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.services.ChatService;
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

@Tag(name = "Chats", description = "Endpoints for managing chats and messages")
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final IMessageService messageService;

    // ------------------- START OR GET CHAT -------------------
    @Operation(
            summary = "Start a chat or get an existing chat",
            description = "If a chat between client and prestataire already exists for the given reservation, it will be returned. Otherwise, a new chat will be created."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat retrieved or created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/start")
    public ResponseEntity<ChatOutputDto> startChat(
            @RequestBody ChatInputDto dto
    ) {
        // Build minimal entity references from IDs
        Client client = Client.builder().id(dto.getClientId()).build();
        Prestataire prestataire = Prestataire.builder().id(dto.getPrestataireId()).build();
        Reservation reservation = Reservation.builder().id(dto.getReservationId()).build();

        Chat chat = chatService.startOrGetChat(client, prestataire, reservation);

        ChatOutputDto output = ChatOutputDto.builder()
                .clientName(chat.getClient().getFirstName())
                .prestataireName(chat.getPrestataire().getFirstName())
                .createdAt(chat.getCreatedAt())
                .active(chat.isActive())
                .build();

        return ResponseEntity.ok(output);
    }

    // ------------------- SEND MESSAGE -------------------
    @Operation(summary = "Send a message in a chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageOutputDto> sendMessage(
            @Parameter(description = "ID of the chat") @PathVariable Long chatId,
            @RequestBody MessageInputDto dto
    ) {
        Message message = messageService.sendMessage(chatId, dto.getSender(), dto.getContenu());

        MessageOutputDto output = MessageOutputDto.builder()
                .senderName(message.getSender().getFirstName())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .read(message.isRead())
                .build();

        return ResponseEntity.ok(output);
    }

    // ------------------- GET MESSAGES -------------------
    @Operation(summary = "Get all messages in a chat, ordered by creation time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found")
    })
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageOutputDto>> getMessages(
            @Parameter(description = "ID of the chat") @PathVariable Long chatId
    ) {
        List<Message> messages = messageService.getMessagesByChatId(chatId);

        List<MessageOutputDto> output = messages.stream()
                .map(m -> MessageOutputDto.builder()
                        .senderName(m.getSender().getFirstName())
                        .content(m.getContent())
                        .sentAt(m.getSentAt())
                        .read(m.isRead())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(output);
    }
}
