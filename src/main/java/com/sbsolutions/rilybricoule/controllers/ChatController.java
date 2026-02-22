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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @PreAuthorize("hasAnyRole('CLIENT', 'PRESTATAIRE', 'ADMIN')")
    public ResponseEntity<ChatOutputDto> startChat(
            @RequestBody ChatInputDto dto
    ) {
        // Build minimal entity references from IDs
        User sender = new User();
        sender.setId(dto.getSenderId());

        User receiver = new User();
        receiver.setId(dto.getReceiverId());

        Reservation reservation = new Reservation();
        reservation.setId(dto.getReservationId());

        Chat chat = chatService.startOrGetChat(sender.getId(), receiver.getId(), reservation.getId());

        ChatOutputDto output = ChatOutputDto.builder()
                .clientName(chat.getClient().getFirstName())
                .prestataireName(chat.getPrestataire().getFirstName())
                .createdAt(chat.getCreatedAt())
                .active(chat.isActive())
                .build();

        return ResponseEntity.ok(output);
    }



}
