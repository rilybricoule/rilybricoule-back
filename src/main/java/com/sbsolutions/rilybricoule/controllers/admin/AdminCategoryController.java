package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AdminCategoryDTO;
import com.sbsolutions.rilybricoule.dto.admin.AdminCategoryStatsDTO;
import com.sbsolutions.rilybricoule.services.AdminCategoryService;
import com.sbsolutions.rilybricoule.services.AdminCategoryService.CategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('CATEGORIES_VIEW')")
    public List<AdminCategoryDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR','SUPPORT') or hasAuthority('CATEGORIES_VIEW')")
    public AdminCategoryStatsDTO getStats() {
        return service.getStats();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('CATEGORIES_MANAGE')")
    public AdminCategoryDTO create(@RequestBody CategoryRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('CATEGORIES_MANAGE')")
    public AdminCategoryDTO update(
            @PathVariable Long id,
            @RequestBody CategoryRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATEUR') or hasAuthority('CATEGORIES_MANAGE')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}