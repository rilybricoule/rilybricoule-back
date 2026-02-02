package com.sbsolutions.rilybricoule.config;

import com.sbsolutions.rilybricoule.user.entity.Permission;
import com.sbsolutions.rilybricoule.user.entity.Role;
import com.sbsolutions.rilybricoule.user.enums.PermissionName;
import com.sbsolutions.rilybricoule.user.enums.RoleName;
import com.sbsolutions.rilybricoule.user.repository.PermissionRepository;
import com.sbsolutions.rilybricoule.user.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public DataInitializer(RoleRepository roleRepository,
                           PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) {

        // 🔹 Create Permissions
        Permission createService = createPermissionIfNotExists(PermissionName.CREATE_SERVICE);
        Permission readService   = createPermissionIfNotExists(PermissionName.READ_SERVICE);
        Permission updateService = createPermissionIfNotExists(PermissionName.UPDATE_SERVICE);
        Permission deleteService = createPermissionIfNotExists(PermissionName.DELETE_SERVICE);

        // 🔹 ROLE_USER → read only
        if (roleRepository.findByName(RoleName.ROLE_USER.name()).isEmpty()) {
            Role userRole = Role.builder()
                    .name(RoleName.ROLE_USER.name())
                    .permissions(Set.of(readService))
                    .build();
            roleRepository.save(userRole);
        }

        // 🔹 ROLE_ADMIN → full access
        if (roleRepository.findByName(RoleName.ROLE_ADMIN.name()).isEmpty()) {
            Role adminRole = Role.builder()
                    .name(RoleName.ROLE_ADMIN.name())
                    .permissions(Set.of(
                            createService,
                            readService,
                            updateService,
                            deleteService
                    ))
                    .build();
            roleRepository.save(adminRole);
        }

        System.out.println("✅ Roles and permissions initialized");
    }

    private Permission createPermissionIfNotExists(PermissionName permissionName) {
        return permissionRepository.findByName(permissionName.name())
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(permissionName.name());
                    return permissionRepository.save(permission);
                });
    }
}
