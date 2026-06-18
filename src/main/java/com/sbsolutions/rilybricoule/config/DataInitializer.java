package com.sbsolutions.rilybricoule.config;

import com.sbsolutions.rilybricoule.entity.Permission;
import com.sbsolutions.rilybricoule.entity.PermissionName;
import com.sbsolutions.rilybricoule.entity.Role;
import com.sbsolutions.rilybricoule.entity.RoleName;
import com.sbsolutions.rilybricoule.repository.PermissionRepository;
import com.sbsolutions.rilybricoule.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        seedAllPermissions();
        seedAllRoles();
        assignPermissionsToAdminRoles();
    }

    private void seedAllPermissions() {
        for (PermissionName permissionName : PermissionName.values()) {
            if (permissionRepository.findByPermissionName(permissionName).isEmpty()) {
                Permission permission = new Permission();
                permission.setPermissionName(permissionName);
                permissionRepository.save(permission);
            }
        }
    }

    private void seedAllRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = new Role();
                role.setRoleName(roleName);
                roleRepository.save(role);
            }
        }
    }

    private void assignPermissionsToAdminRoles() {
        Role superAdmin = roleRepository.findByRoleName(RoleName.ROLE_SUPER_ADMIN).orElseThrow();
        Role moderateur = roleRepository.findByRoleName(RoleName.ROLE_MODERATEUR).orElseThrow();
        Role support = roleRepository.findByRoleName(RoleName.ROLE_SUPPORT).orElseThrow();

        superAdmin.setPermissions(getPermissions(
                PermissionName.DASHBOARD_VIEW,
                PermissionName.CLIENTS_VIEW,
                PermissionName.CLIENTS_EDIT,
                PermissionName.CLIENTS_DELETE,
                PermissionName.PROVIDERS_VIEW,
                PermissionName.PROVIDERS_APPROVE,
                PermissionName.PROVIDERS_SUSPEND,
                PermissionName.RESERVATIONS_VIEW,
                PermissionName.RESERVATIONS_INTERVENE,
                PermissionName.PAYMENTS_VIEW,
                PermissionName.PAYMENTS_MANAGE,
                PermissionName.CATEGORIES_VIEW,
                PermissionName.CATEGORIES_MANAGE,
                PermissionName.PROMOS_VIEW,
                PermissionName.PROMOS_MANAGE,
                PermissionName.NOTIFICATIONS_SEND_ALL,
                PermissionName.NOTIFICATIONS_SEND_TARGETED,
                PermissionName.TICKETS_VIEW,
                PermissionName.TICKETS_RESPOND,
                PermissionName.TICKETS_MANAGE,
                PermissionName.CONTENT_VIEW,
                PermissionName.NOTIFICATIONS_VIEW,
                PermissionName.CONTENT_MODERATE,
                PermissionName.SETTINGS_VIEW,
                PermissionName.SETTINGS_MANAGE,
                PermissionName.ADMINS_VIEW,
                PermissionName.ADMINS_MANAGE,
                PermissionName.AUDIT_VIEW_ALL,
                PermissionName.AUDIT_VIEW_OWN,
                PermissionName.DATA_DELETE
        ));

        moderateur.setPermissions(getPermissions(
                PermissionName.DASHBOARD_VIEW,
                PermissionName.CLIENTS_VIEW,
                PermissionName.CLIENTS_EDIT,
                PermissionName.PROVIDERS_VIEW,
                PermissionName.PROVIDERS_APPROVE,
                PermissionName.PROVIDERS_SUSPEND,
                PermissionName.RESERVATIONS_VIEW,
                PermissionName.RESERVATIONS_INTERVENE,
                PermissionName.PAYMENTS_VIEW,
                PermissionName.CATEGORIES_VIEW,
                PermissionName.NOTIFICATIONS_VIEW,
                PermissionName.NOTIFICATIONS_SEND_TARGETED,
                PermissionName.TICKETS_VIEW,
                PermissionName.TICKETS_RESPOND,
                PermissionName.TICKETS_MANAGE,
                PermissionName.CONTENT_VIEW,
                PermissionName.CONTENT_MODERATE,
                PermissionName.AUDIT_VIEW_OWN
        ));

        support.setPermissions(getPermissions(
                PermissionName.DASHBOARD_VIEW,
                PermissionName.CLIENTS_VIEW,
                PermissionName.PROVIDERS_VIEW,
                PermissionName.NOTIFICATIONS_VIEW,
                PermissionName.RESERVATIONS_VIEW,
                PermissionName.TICKETS_VIEW,
                PermissionName.TICKETS_RESPOND
        ));

        roleRepository.save(superAdmin);
        roleRepository.save(moderateur);
        roleRepository.save(support);
    }

    private Set<Permission> getPermissions(PermissionName... permissionNames) {
        Set<Permission> permissions = new HashSet<>();

        Arrays.stream(permissionNames).forEach(permissionName -> {
            Permission permission = permissionRepository.findByPermissionName(permissionName)
                    .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));
            permissions.add(permission);
        });

        return permissions;
    }
}
