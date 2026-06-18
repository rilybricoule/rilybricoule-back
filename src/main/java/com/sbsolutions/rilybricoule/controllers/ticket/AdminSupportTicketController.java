package com.sbsolutions.rilybricoule.controllers.ticket;

import com.sbsolutions.rilybricoule.dto.tickets.AdminSupportTicketDTO;
import com.sbsolutions.rilybricoule.dto.tickets.ResolveTicketRequestDTO;
import com.sbsolutions.rilybricoule.services.ticket.AdminSupportTicketService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
public class AdminSupportTicketController {

    private final AdminSupportTicketService adminSupportTicketService;

    @GetMapping
    @PreAuthorize("hasAuthority('TICKETS_VIEW')")
    public List<AdminSupportTicketDTO> getTickets() {
        return adminSupportTicketService.getTickets();
    }

    @PostMapping("/{id:\\d+}/reply")
    @PreAuthorize("hasAuthority('TICKETS_RESPOND') or hasAuthority('TICKETS_MANAGE')")
    public AdminSupportTicketDTO reply(
            @PathVariable Long id,
            @RequestBody MessageRequest request
    ) {
        return adminSupportTicketService.reply(id, request.getMessage());
    }

    @PatchMapping("/{id:\\d+}/status")
    @PreAuthorize("hasAuthority('TICKETS_MANAGE') or hasAuthority('TICKETS_RESPOND')")
    public AdminSupportTicketDTO updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request
    ) {
        return adminSupportTicketService.updateStatus(id, request.getStatus());
    }

    @PatchMapping("/{id:\\d+}/category")
    @PreAuthorize("hasAuthority('TICKETS_MANAGE')")
    public AdminSupportTicketDTO updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request
    ) {
        return adminSupportTicketService.updateCategory(id, request.getCategory());
    }

    @PatchMapping("/{id:\\d+}/litige")
    @PreAuthorize("hasAuthority('TICKETS_MANAGE')")
    public AdminSupportTicketDTO updateLitige(
            @PathVariable Long id,
            @RequestBody LitigeRequest request
    ) {
        return adminSupportTicketService.updateLitige(id, request.isLitige());
    }

    @PatchMapping("/{id:\\d+}/admin-note")
    @PreAuthorize("hasAuthority('TICKETS_MANAGE') or hasAuthority('TICKETS_RESPOND')")
    public AdminSupportTicketDTO updateAdminNote(
            @PathVariable Long id,
            @RequestBody NoteRequest request
    ) {
        return adminSupportTicketService.updateAdminNote(id, request.getNote());
    }

    @PostMapping("/{id:\\d+}/resolve")
    @PreAuthorize("hasAuthority('TICKETS_MANAGE')")
    public AdminSupportTicketDTO resolve(
            @PathVariable Long id,
            @RequestBody ResolveTicketRequestDTO request
    ) {
        return adminSupportTicketService.resolve(
                id,
                request.getAction(),
                request.getNote(),
                request.getNewPrestataireId()
        );
    }


    @Data
    public static class MessageRequest {
        private String message;
    }

    @Data
    public static class StatusRequest {
        private String status;
    }

    @Data
    public static class CategoryRequest {
        private String category;
    }

    @Data
    public static class LitigeRequest {
        private boolean litige;
    }

    @Data
    public static class NoteRequest {
        private String note;
    }

    @Data
    public static class ResolveRequest {
        private String action;
        private String note;
    }
}
