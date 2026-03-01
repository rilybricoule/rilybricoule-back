package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.NotificationPreferenceDto;
import com.sbsolutions.rilybricoule.services.InotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification Preferences", description = "Manage user notification preferences (mute/unmute)")
@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final InotificationPreferenceService notificationPreferenceService;

    @Operation(
            summary = "Get notification preferences for a user",
            description = "Returns user preferences. Creates default preferences if none exist yet."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<NotificationPreferenceDto> getPreferences(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return ResponseEntity.ok(notificationPreferenceService.getOrCreate(userId));
    }

    @Operation(
            summary = "Update notification preferences (partial)",
            description = "Updates only provided fields (null fields are ignored)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{userId}")
    public ResponseEntity<NotificationPreferenceDto> updatePreferences(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @RequestBody NotificationPreferenceDto dto) {
        return ResponseEntity.ok(notificationPreferenceService.update(userId, dto));
    }
}
