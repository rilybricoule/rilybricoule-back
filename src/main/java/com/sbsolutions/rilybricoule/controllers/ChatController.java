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

    @Operation(summary = "Get active chats by user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatOutputDto>> getChatsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getChatsByUserId(userId));
    }

    @Operation(summary = "Pin conversation", description = "Pins the chat for the requesting participant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation pinned"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "Not a participant")
    })
    @PutMapping("/{chatId}/pin")
    public ResponseEntity<Void> pinConversation(
            @Parameter(description = "Chat ID") @PathVariable Long chatId,
            @Parameter(description = "User ID requesting pin") @RequestParam Long userId) {
        chatService.pinConversation(chatId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purge-deleted")
    public ResponseEntity<Integer> purgeDeletedChats() {
        int count = messageService.purgeDeletedMessagesOlderThanSevenDays();
        return ResponseEntity.ok(count);
    }


    @Operation(summary = "Unpin conversation", description = "Removes pin from the chat for the requesting participant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation unpinned"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "Not a participant")
    })
    @PutMapping("/{chatId}/unpin")
    public ResponseEntity<Void> unpinConversation(
            @Parameter(description = "Chat ID") @PathVariable Long chatId,
            @Parameter(description = "User ID requesting unpin") @RequestParam Long userId) {
        chatService.unpinConversation(chatId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Archive conversation", description = "Moves the chat to archive. It will no longer appear in the main chat list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation archived"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "Not a participant")
    })
    @PutMapping("/{chatId}/archive")
    public ResponseEntity<Void> archiveConversation(
            @Parameter(description = "Chat ID") @PathVariable Long chatId,
            @RequestParam Long userId) {
        chatService.archiveConversation(chatId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Unarchive conversation",
            description = "Restores an archived chat to the active chat list for the requesting participant."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation unarchived"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "Not a participant")
    })
    @PutMapping("/{chatId}/unarchive")
    public ResponseEntity<Void> unarchiveConversation(
            @Parameter(description = "Chat ID") @PathVariable Long chatId,
            @Parameter(description = "User ID requesting unarchive") @RequestParam Long userId) {
        chatService.unarchiveConversation(chatId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Restore deleted conversation",
            description = "Restores a soft-deleted chat for the requesting participant if deletion was within the last 7 days."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation restored"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "Not a participant"),
            @ApiResponse(responseCode = "409", description = "Restore window expired")
    })
    @PutMapping("/{chatId}/restore")
    public ResponseEntity<Void> restoreDeletedConversation(
            @Parameter(description = "Chat ID") @PathVariable Long chatId,
            @Parameter(description = "User ID requesting restore") @RequestParam Long userId) {
        chatService.restoreDeletedConversation(chatId, userId);
        return ResponseEntity.ok().build();
    }





    @Operation(
            summary = "Delete conversation",
            description = "Permanently deletes the chat and all its messages. Only a participant can delete."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conversation deleted"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "Not a participant")
    })
    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteConversation(
            @Parameter(description = "Chat ID") @PathVariable Long chatId,
            @Parameter(description = "User ID requesting deletion") @RequestParam Long userId) {
        chatService.deleteConversation(chatId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Disable conversation", description = "Disables the chat for the given user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation disabled"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "Not a participant")
    })
    @PutMapping("/{chatId}/disable")
    public ResponseEntity<Void> disableConversation(
            @Parameter(description = "Chat ID") @PathVariable Long chatId,
            @RequestParam Long userId) {
        chatService.disableConversation(chatId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Enable conversation", description = "Re-enables the chat for the given user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation enabled"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "Not a participant")
    })
    @PutMapping("/{chatId}/enable")
    public ResponseEntity<Void> enableConversation(
            @Parameter(description = "Chat ID") @PathVariable Long chatId,
            @RequestParam Long userId) {
        chatService.enableConversation(chatId, userId);
        return ResponseEntity.ok().build();
    }





}
