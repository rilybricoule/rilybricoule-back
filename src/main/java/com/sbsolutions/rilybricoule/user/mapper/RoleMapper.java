package com.sbsolutions.rilybricoule.user.mapper;

import com.sbsolutions.rilybricoule.user.dto.RoleDto;
import com.sbsolutions.rilybricoule.user.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public static RoleDto toDto(Role role) {
        if (role == null) return null;

        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .createdBy(role.getCreatedBy())
                .createdDate(role.getCreatedDate())
                .build();
    }

    public static Role toEntity(RoleDto dto) {
        if (dto == null) return null;

        return Role.builder()
                .id(dto.getId())
                .name(dto.getName())
                .createdBy(dto.getCreatedBy())
                .createdDate(dto.getCreatedDate())
                .build();
    }
}
