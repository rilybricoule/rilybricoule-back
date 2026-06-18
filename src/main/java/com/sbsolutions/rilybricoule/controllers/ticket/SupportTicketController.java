package com.sbsolutions.rilybricoule.controllers.ticket;

import com.sbsolutions.rilybricoule.dto.tickets.AdminSupportTicketDTO;
import com.sbsolutions.rilybricoule.dto.tickets.CreateSupportTicketRequest;
import com.sbsolutions.rilybricoule.services.ticket.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support/tickets")
@RequiredArgsConstructor
@Tag(name = "Support Tickets", description = "Endpoints for clients and prestataires to manage their support tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a support ticket")
    public AdminSupportTicketDTO createTicket(@RequestBody CreateSupportTicketRequest request) {
        return supportTicketService.createTicket(request);
    }

    @GetMapping("/mine")
    @Operation(summary = "Get my support tickets")
    public List<AdminSupportTicketDTO> getMyTickets() {
        return supportTicketService.getMyTickets();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get my support ticket by id")
    public AdminSupportTicketDTO getMyTicketById(@PathVariable Long id) {
        return supportTicketService.getMyTicketById(id);
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "Reply to my support ticket")
    public AdminSupportTicketDTO replyToMyTicket(
            @PathVariable Long id,
            @RequestBody MessageRequest request
    ) {
        return supportTicketService.replyToMyTicket(id, request.getMessage());
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close my support ticket")
    public AdminSupportTicketDTO closeMyTicket(@PathVariable Long id) {
        return supportTicketService.closeMyTicket(id);
    }

    @Data
    public static class MessageRequest {
        private String message;
    }
}
