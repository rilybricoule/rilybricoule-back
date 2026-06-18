package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AdminClientDTO;
import com.sbsolutions.rilybricoule.services.AdminClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/clients")
@RequiredArgsConstructor
public class AdminClientController {

    private final AdminClientService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('CLIENTS_VIEW')")
    public List<AdminClientDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('CLIENTS_VIEW')")
    public AdminClientDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('CLIENTS_EDIT')")
    public AdminClientDTO activate(@PathVariable Long id) {
        return service.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('CLIENTS_EDIT')")
    public AdminClientDTO deactivate(@PathVariable Long id) {
        return service.deactivate(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('CLIENTS_EDIT')")
    public AdminClientDTO create(@RequestBody AdminClientDTO request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('CLIENTS_EDIT')")
    public AdminClientDTO update(
            @PathVariable Long id,
            @RequestBody AdminClientDTO request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('CLIENTS_EDIT')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
