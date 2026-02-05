package com.sbsolutions.rilybricoule.user.service.impl;

import com.sbsolutions.rilybricoule.user.dto.RoleDto;
import com.sbsolutions.rilybricoule.user.entity.Role;
import com.sbsolutions.rilybricoule.user.entity.User;
import com.sbsolutions.rilybricoule.user.repository.RoleRepository;
import com.sbsolutions.rilybricoule.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolesServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RolesServiceImpl rolesService;

    private Role role;
    private RoleDto roleDto;
    private User user;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .createdBy("SYSTEM")
                .createdDate(LocalDateTime.now())
                .build();

        roleDto = new RoleDto();
        roleDto.setId(1L);
        roleDto.setName("ROLE_ADMIN");

        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .password("12345")
                .roles(new HashSet<>()) // <-- initialize here
                .build();
    }

    @Test
    void testCreateRole() {
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleDto created = rolesService.createRole(roleDto);

        assertNotNull(created);
        assertEquals("ROLE_ADMIN", created.getName());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void testGetAllRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<RoleDto> roles = rolesService.getAllRoles();

        assertEquals(1, roles.size());
        assertEquals("ROLE_ADMIN", roles.get(0).getName());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void testAssignRolesToUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(List.of(1L))).thenReturn(List.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);

        rolesService.assignRolesToUser(1L, List.of(1L));

        assertTrue(user.getRoles().contains(role));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testAssignRolesToUser_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                rolesService.assignRolesToUser(1L, List.of(1L))
        );

        assertEquals("User not found with id: 1", ex.getMessage());
    }

    @Test
    void testAssignRolesToUser_RoleNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(List.of(1L))).thenReturn(List.of()); // empty list

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                rolesService.assignRolesToUser(1L, List.of(1L))
        );

        assertEquals("One or more roles not found", ex.getMessage());
    }
}
