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
}
