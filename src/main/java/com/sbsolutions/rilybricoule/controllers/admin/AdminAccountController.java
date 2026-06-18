package com.sbsolutions.rilybricoule.controllers.admin;

import com.sbsolutions.rilybricoule.dto.admin.AdminAccountDTO;
import com.sbsolutions.rilybricoule.dto.admin.CreateAdminAccountRequest;
import com.sbsolutions.rilybricoule.dto.admin.UpdateAdminAccountRequest;
import com.sbsolutions.rilybricoule.services.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMINS_VIEW') or hasAuthority('ADMINS_MANAGE')")
    public List<AdminAccountDTO> getAll() {
        return adminAccountService.getAllAdmins();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMINS_MANAGE')")
    public AdminAccountDTO create(@RequestBody CreateAdminAccountRequest request) {
        return adminAccountService.createAdmin(request);
    }
    @PatchMapping("/{id}/toggle-2fa")
    @PreAuthorize("hasAuthority('ADMINS_MANAGE')")
    public AdminAccountDTO toggle2FA(@PathVariable Long id) {
        return adminAccountService.toggle2FA(id);
    }


    @PostMapping("/{id}/force-logout")
    @PreAuthorize("hasAuthority('ADMINS_MANAGE')")
    public Map<String, String> forceLogout(@PathVariable Long id) {
        adminAccountService.forceLogout(id);
        return Map.of("message", "Admin logged out successfully");
    }

    @PostMapping("/{id}/password-reset-email")
    @PreAuthorize("hasAuthority('ADMINS_MANAGE')")
    public Map<String, String> sendPasswordResetEmail(@PathVariable Long id) {
        adminAccountService.sendPasswordResetEmail(id);
        return Map.of("message", "Password reset required");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINS_MANAGE')")
    public AdminAccountDTO update(
            @PathVariable Long id,
            @RequestBody UpdateAdminAccountRequest request
    ) {
        return adminAccountService.updateAdmin(id, request);
    }



    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMINS_MANAGE')")
    public AdminAccountDTO updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateAdminStatusRequest request
    ) {
        return adminAccountService.updateStatus(id, request.isEnabled());
    }

    public static class UpdateAdminStatusRequest {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}