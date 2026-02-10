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
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        for (PermissionName pName : PermissionName.values()) {
            if (permissionRepository.findByPermissionName(pName).isEmpty()) {
                Permission permission = new Permission();
                permission.setPermissionName(pName);
                permissionRepository.save(permission);
            }
        }

        if (roleRepository.findByRoleName(RoleName.ROLE_CLIENT).isEmpty()) {
            Set<Permission> clientPermissions = Arrays.asList(
                    PermissionName.READ_SERVICE,
                    PermissionName.CREATE_RESERVATION,
                    PermissionName.READ_RESERVATION,
                    PermissionName.CANCEL_RESERVATION
            ).stream()
                    .map(p -> permissionRepository.findByPermissionName(p).orElseThrow())
                    .collect(Collectors.toSet());

            Role clientRole = new Role();
            clientRole.setRoleName(RoleName.ROLE_CLIENT);
            clientRole.setPermissions(clientPermissions);
            roleRepository.save(clientRole);
        }

        if (roleRepository.findByRoleName(RoleName.ROLE_PRESTATAIRE).isEmpty()) {
            Set<Permission> prestaPermissions = Arrays.asList(
                    PermissionName.READ_SERVICE,
                    PermissionName.MANAGE_OWN_SERVICES,
                    PermissionName.READ_RESERVATION,
                    PermissionName.ACCEPT_RESERVATION,
                    PermissionName.REFUSE_RESERVATION
            ).stream()
                    .map(p -> permissionRepository.findByPermissionName(p).orElseThrow())
                    .collect(Collectors.toSet());

            Role prestaRole = new Role();
            prestaRole.setRoleName(RoleName.ROLE_PRESTATAIRE);
            prestaRole.setPermissions(prestaPermissions);
            roleRepository.save(prestaRole);
        }

        if (roleRepository.findByRoleName(RoleName.ROLE_ADMIN).isEmpty()) {
            Set<Permission> allPermissions = permissionRepository.findAll()
                    .stream().collect(Collectors.toSet());

            Role adminRole = new Role();
            adminRole.setRoleName(RoleName.ROLE_ADMIN);
            adminRole.setPermissions(allPermissions);
            roleRepository.save(adminRole);
        }
    }
}
