package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.input.ChatInputDto;
import com.sbsolutions.rilybricoule.dto.input.MessageInputDto;
import com.sbsolutions.rilybricoule.dto.output.ChatOutputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.services.ChatService;
import com.sbsolutions.rilybricoule.services.IChatService;
import com.sbsolutions.rilybricoule.services.IMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Chats", description = "Endpoints for managing chats and messages")
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final IChatService chatService;
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
    public ResponseEntity<ChatOutputDto> startChat(@RequestBody ChatInputDto dto) {
        // Build minimal entity references from IDs
        Chat chat = chatService.startOrGetChat(dto.getClientId(), dto.getPrestataireId(), dto.getReservationId());

        // Map messages
        List<MessageOutputDto> messageDtos = Optional.ofNullable(chat.getMessages())
                .orElse(Collections.emptyList())
                .stream()
                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                .map(msg -> MessageOutputDto.builder()
                        .id(msg.getId())
                        .senderName(msg.getSender().getFirstName())
                        .content(msg.getContent())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());




        // Build output
        ChatOutputDto output = ChatOutputDto.builder()
                .chatId(chat.getId())
                .clientId(chat.getClient().getId())
                .prestataireId(chat.getPrestataire().getId())
                .clientFirstName(chat.getClient().getFirstName())
                .clientLastName(chat.getClient().getLastName())
                .prestataireFirstName(chat.getPrestataire().getFirstName())
                .prestataireLastName(chat.getPrestataire().getLastName())
                .reservationId(chat.getReservation() != null ? chat.getReservation().getId() : null)
                .createdAt(chat.getCreatedAt())
                .active(chat.isActive())
                .lastMessageAt(chat.getLastMessageAt())
                .messages(messageDtos)
                .build();

        return ResponseEntity.ok(output);
    }



}
