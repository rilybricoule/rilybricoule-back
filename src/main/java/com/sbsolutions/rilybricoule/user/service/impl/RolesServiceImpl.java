package com.sbsolutions.rilybricoule.user.service.impl;

import com.sbsolutions.rilybricoule.user.dto.RoleDto;
import com.sbsolutions.rilybricoule.user.entity.Role;
import com.sbsolutions.rilybricoule.user.entity.User;
import com.sbsolutions.rilybricoule.user.mapper.RoleMapper;
import com.sbsolutions.rilybricoule.user.repository.RoleRepository;
import com.sbsolutions.rilybricoule.user.repository.UserRepository;
import com.sbsolutions.rilybricoule.user.service.RolesService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolesServiceImpl implements RolesService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;


    public RolesServiceImpl (RoleRepository roleRepository , RoleMapper roleMapper , UserRepository userRepository)
        {

            this.roleRepository=roleRepository;
            this.userRepository=userRepository;
        }


    @Override
    public RoleDto createRole(RoleDto roleDto) {

        roleRepository.findByName(roleDto.getName()).ifPresent(r -> {
            throw new IllegalArgumentException("Role already exists");
        });

        Role role = RoleMapper.toEntity(roleDto);
        role = roleRepository.save(role);

        return RoleMapper.toDto(role);
    }

    @Override
    public List<RoleDto> getAllRoles() {
        // Fetch all roles from DB
        List<Role> roles = roleRepository.findAll();

        // Convert to DTOs
        return roles.stream()
                .map(RoleMapper::toDto)
                .toList();
    }

    @Override
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // Fetch the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        //  Fetch all roles
        List<Role> roles = roleRepository.findAllById(roleIds);

        if (roles.size() != roleIds.size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }

        // Add roles to user's existing roles
        user.getRoles().addAll(roles);

        //  Save user
        userRepository.save(user);
    }

}
