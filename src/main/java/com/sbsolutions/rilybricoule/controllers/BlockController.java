package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.services.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Blocks", description = "Block / unblock users for chat messaging")
@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @Operation(summary = "Block a user")
    @PostMapping
    public ResponseEntity<Void> blockUser(@RequestParam Long blockerId, @RequestParam Long blockedId) {
        blockService.blockUser(blockerId, blockedId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Unblock a user")
    @DeleteMapping
    public ResponseEntity<Void> unblockUser(@RequestParam Long blockerId, @RequestParam Long blockedId) {
        blockService.unblockUser(blockerId, blockedId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check if users are blocked (either direction)")
    @GetMapping("/check")
    public ResponseEntity<Boolean> isBlockedEitherWay(@RequestParam Long userA, @RequestParam Long userB) {
        return ResponseEntity.ok(blockService.isBlockedEitherWay(userA, userB));
    }
}