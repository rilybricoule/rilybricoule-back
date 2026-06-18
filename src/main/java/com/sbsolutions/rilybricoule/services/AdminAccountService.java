package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AdminAccountDTO;
import com.sbsolutions.rilybricoule.dto.admin.UpdateAdminAccountRequest;
import com.sbsolutions.rilybricoule.entity.Permission;
import com.sbsolutions.rilybricoule.entity.Role;
import com.sbsolutions.rilybricoule.entity.RoleName;
import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbsolutions.rilybricoule.dto.admin.CreateAdminAccountRequest;
import com.sbsolutions.rilybricoule.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import com.sbsolutions.rilybricoule.security.domain.port.out.RefreshTokenRepositoryPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Map;

import java.util.HashSet;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<AdminAccountDTO> getAllAdmins() {
        return userRepository.findAll()
                .stream()
                .filter(this::isAdminUser)
                .sorted(Comparator.comparing(
                        User::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(this::toDTO)
                .toList();
    }

    private boolean isAdminUser(User user) {
        return user.getRoles() != null &&
                user.getRoles().stream().anyMatch(role ->
                        role.getRoleName() == RoleName.ROLE_ADMIN ||
                                role.getRoleName() == RoleName.ROLE_SUPER_ADMIN ||
                                role.getRoleName() == RoleName.ROLE_MODERATEUR ||
                                role.getRoleName() == RoleName.ROLE_SUPPORT
                );
    }
    @Transactional
    public AdminAccountDTO createAdmin(CreateAdminAccountRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        RoleName roleName = parseAdminRole(request.getRoleName());

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Role not found: " + roleName
                ));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(role);

        User saved = userRepository.save(user);

        return toDTO(saved);
    }


    private RoleName parseAdminRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roleName is required");
        }

        String normalized = roleName.trim().toUpperCase();

        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }

        try {
            RoleName parsed = RoleName.valueOf(normalized);

            if (parsed == RoleName.ROLE_SUPER_ADMIN ||
                    parsed == RoleName.ROLE_MODERATEUR ||
                    parsed == RoleName.ROLE_SUPPORT) {
                return parsed;
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid admin role. Use ROLE_SUPER_ADMIN, ROLE_MODERATEUR, or ROLE_SUPPORT"
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid roleName: " + roleName);
        }
    }

    @Transactional
    public AdminAccountDTO updateAdmin(Long id, UpdateAdminAccountRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"
                ));

        if (!isAdminUser(user)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an admin account"
            );
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Email already exists"
                );
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getRoleName() != null) {
            RoleName roleName = parseAdminRole(request.getRoleName());

            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Role not found: " + roleName
                    ));

            user.getRoles().removeIf(r ->
                    r.getRoleName() == RoleName.ROLE_ADMIN ||
                            r.getRoleName() == RoleName.ROLE_SUPER_ADMIN ||
                            r.getRoleName() == RoleName.ROLE_MODERATEUR ||
                            r.getRoleName() == RoleName.ROLE_SUPPORT
            );

            user.getRoles().add(role);
        }

        User saved = userRepository.save(user);

        return toDTO(saved);
    }



    @Transactional
    public void sendPasswordResetEmail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"
                ));

        if (!isAdminUser(user)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an admin account"
            );
        }

        user.setMustChangePassword(true);
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUser(user);

        messagingTemplate.convertAndSend(
                "/topic/force-logout/" + user.getEmail(),
                Map.of(
                        "type", "PASSWORD_RESET_REQUIRED",
                        "email", user.getEmail()
                )
        );
    }

    @Transactional
    public void forceLogout(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"
                ));

        if (!isAdminUser(user)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an admin account"
            );
        }

        refreshTokenRepository.revokeAllByUser(user);

        messagingTemplate.convertAndSend(
                "/topic/force-logout/" + user.getEmail(),
                Map.of(
                        "type", "FORCE_LOGOUT",
                        "email", user.getEmail()
                )
        );
    }

    @Transactional
    public AdminAccountDTO toggle2FA(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"
                ));

        if (!isAdminUser(user)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an admin account"
            );
        }

        user.setTwoFAEnabled(!user.isTwoFAEnabled());

        User saved = userRepository.save(user);

        return toDTO(saved);
    }

    @Transactional
    public AdminAccountDTO updateStatus(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"
                ));

        if (!isAdminUser(user)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an admin account"
            );
        }

        user.setEnabled(enabled);

        User saved = userRepository.save(user);

        return toDTO(saved);
    }

    private AdminAccountDTO toDTO(User user) {
        Role role = user.getRoles()
                .stream()
                .filter(r -> r.getRoleName() == RoleName.ROLE_SUPER_ADMIN)
                .findFirst()
                .orElse(user.getRoles().iterator().next());

        List<String> permissions = role.getPermissions()
                .stream()
                .map(Permission::getPermissionName)
                .map(Enum::name)
                .sorted()
                .toList();

        return AdminAccountDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .photoUrl(user.getPhotoUrl())
                .enabled(user.isEnabled())
                .twoFAEnabled(user.isTwoFAEnabled())
                .roleName(role.getRoleName().name())
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}