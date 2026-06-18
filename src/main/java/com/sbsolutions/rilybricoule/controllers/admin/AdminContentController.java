package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.version.AppVersionDTO;
import com.sbsolutions.rilybricoule.services.AppVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/content/app-versions")
@RequiredArgsConstructor
public class AdminContentController {

    private final AppVersionService appVersionService;

    @GetMapping
    @PreAuthorize("hasAuthority('CONTENT_VIEW')")
    public List<AppVersionDTO> getAll() {
        return appVersionService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONTENT_MANAGE')")
    public AppVersionDTO create(@RequestBody AppVersionDTO request) {
        return appVersionService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTENT_MANAGE')")
    public AppVersionDTO update(@PathVariable Long id, @RequestBody AppVersionDTO request) {
        return appVersionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTENT_MANAGE')")
    public void delete(@PathVariable Long id) {
        appVersionService.delete(id);
    }
}