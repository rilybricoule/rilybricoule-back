package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.input.MessageInputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.services.IMessageService;
import com.sbsolutions.rilybricoule.services.MessageArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Chat messages management")
public class MessageController {

    private final IMessageService messageService;
    private final MessageArchiveService messageArchiveService;
    // ------------------- SEND MESSAGE -------------------
    @Operation(summary = "Send a message in a chat",
            description = "Saves a message to a chat and optionally triggers notifications.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/send")
    public ResponseEntity<MessageOutputDto> sendMessage(
            @RequestBody
            MessageInputDto dto
    ) {
        MessageOutputDto output = messageService.sendMessage(
                dto.getChatId(),
                dto.getSenderId(),
                dto.getContent()
        );
        return ResponseEntity.ok(output);
    }

    // ------------------- GET MESSAGES BY CHAT -------------------
    @Operation(summary = "Get all messages in a chat, ordered by creation time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found")
    })
    @GetMapping("/{chatId}")
    public ResponseEntity<List<MessageOutputDto>> getMessagesByChat(
            @Parameter(description = "ID of the chat to retrieve messages for") @PathVariable Long chatId
    ) {
        List<MessageOutputDto> messages = messageService.getMessagesByChatId(chatId);
        return ResponseEntity.ok(messages);
    }

    // ------------------- SAVE MESSAGE -------------------
    @Operation(summary = "Save a message in a chat",
            description = "Saves a message to a chat using input DTO. Determines receiver automatically.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message saved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/save")
    public ResponseEntity<MessageOutputDto> saveMessage(
            @RequestBody
            MessageInputDto dto
    ) {
        MessageOutputDto output = messageService.saveMessage(
                dto.getChatId(),
                dto.getSenderId(),
                dto
        );
        return ResponseEntity.ok(output);
    }
    // ------------------- UNREAD COUNT -------------------
    @Operation(summary = "Get total unread message count for a user",
            description = "Returns the number of messages received and not yet read by the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count returned successfully")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(messageService.getUnreadMessageCount(userId));
    }

    @Operation(summary = "Get unread message count for a chat",
            description = "Returns the number of unread messages in the chat for the given user (as receiver).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count returned successfully")
    })
    @GetMapping("/chats/{chatId}/unread-count")
    public ResponseEntity<Long> getUnreadCountForChat(
            @Parameter(description = "ID of the chat") @PathVariable Long chatId,
            @RequestParam Long receiverId) {
        return ResponseEntity.ok(messageService.getUnreadMessageCountForChat(chatId, receiverId));
    }
    @Operation(summary = "Trigger archive now (for testing)",
            description = "Archives messages older than 6 months. Optional: use olderThanMinutes (e.g. 1) to archive messages older than that many minutes for easy testing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archive job ran successfully")
    })
    @PostMapping("/archive-now")
    public ResponseEntity<Integer> archiveNow(
            @Parameter(description = "Optional. If set, archive messages older than this many minutes (e.g. 1 for testing). If not set, uses 6 months.")
            @RequestParam(required = false) Integer olderThanMinutes) {
        int archived;
        if (olderThanMinutes != null) {
            archived = messageArchiveService.archiveOlderThanMinutes(olderThanMinutes);
        } else {
            messageArchiveService.archiveOldMessages();
            archived = -1; // or you can change archiveOldMessages() to return int
        }
        return ResponseEntity.ok(archived);
    }


    @Operation(summary = "Mark all messages in a chat as read")
    @PostMapping("/chats/{chatId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long chatId,
                                           @RequestParam Long receiverId) {

        messageService.markAsRead(chatId, receiverId);
        return ResponseEntity.ok().build();
    }

    // ------------------- EDIT MESSAGE -------------------
    @Operation(summary = "Edit a message",
            description = "Updates the content of a message. Only the sender can edit.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message updated successfully"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
            @ApiResponse(responseCode = "403", description = "Not allowed to edit this message")
    })
    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageOutputDto> editMessage(
            @Parameter(description = "ID of the message to edit") @PathVariable Long messageId,
            @RequestParam Long senderId,
            @RequestBody MessageInputDto dto
    ) {
        MessageOutputDto output = messageService.editMessage(messageId, senderId, dto.getContent());
        return ResponseEntity.ok(output);
    }

    // ------------------- DELETE MESSAGE -------------------
    @Operation(summary = "Delete a message (soft delete)",
            description = "Marks a message as deleted. Only the sender can delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
            @ApiResponse(responseCode = "403", description = "Not allowed to delete this message")
    })
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "ID of the message to delete") @PathVariable Long messageId,
            @RequestParam Long senderId
    ) {
        messageService.deleteMessage(messageId, senderId);
        return ResponseEntity.ok().build();
    }
}
