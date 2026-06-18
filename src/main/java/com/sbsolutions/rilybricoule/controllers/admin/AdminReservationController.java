package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AdminReservationDTO;
import com.sbsolutions.rilybricoule.services.AdminReservationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final AdminReservationService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MODERATEUR', 'SUPPORT') or hasAuthority('RESERVATIONS_VIEW')")
    public List<AdminReservationDTO> getAll() {
        return service.getAll();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MODERATEUR') or hasAuthority('RESERVATIONS_INTERVENE')")
    public AdminReservationDTO updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request
    ) {
        return service.updateStatus(id, request.getStatus());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MODERATEUR') or hasAuthority('RESERVATIONS_INTERVENE')")
    public AdminReservationDTO cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @PatchMapping("/{id}/note")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MODERATEUR', 'SUPPORT') or hasAuthority('RESERVATIONS_INTERVENE')")
    public AdminReservationDTO updateNote(
            @PathVariable Long id,
            @RequestBody UpdateNoteRequest request
    ) {
        return service.updateNote(id, request.getNote());
    }

    @Data
    static class UpdateStatusRequest {
        private String status;
    }

    @Data
    static class UpdateNoteRequest {
        private String note;
    }
}