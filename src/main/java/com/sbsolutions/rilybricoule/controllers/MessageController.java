package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.input.MessageInputDto;
import com.sbsolutions.rilybricoule.dto.output.MessageOutputDto;
import com.sbsolutions.rilybricoule.services.IMessageService;
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
                dto
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

    @Operation(
            summary = "Restore conversation",
            description = "Restores all messages in the chat that were deleted within the last 7 days. After 7 days, messages are permanently deleted and cannot be restored."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation restored successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "User is not part of this chat")
    })
    @PutMapping("/chats/{chatId}/restore")
    public ResponseEntity<Void> restoreConversation(
            @Parameter(description = "ID of the chat to restore") @PathVariable Long chatId,
            @Parameter(description = "ID of the user requesting the restore (must be a participant)") @RequestParam Long userId) {
        messageService.restoreConversation(chatId, userId);
        return ResponseEntity.ok().build();
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


    @Operation(
            summary = "Purge old deleted messages",
            description = "Permanently deletes messages that were soft-deleted more than 7 days ago. Can be called manually or by a scheduler."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purge completed; body = number of messages deleted"),
    })
    @PostMapping("/purge-deleted")
    public ResponseEntity<Integer> purgeDeletedMessages() {
        int count = messageService.purgeDeletedMessagesOlderThanSevenDays();
        return ResponseEntity.ok(count);
    }


}
