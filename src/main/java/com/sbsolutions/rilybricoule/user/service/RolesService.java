package com.sbsolutions.rilybricoule.user.service;

import com.sbsolutions.rilybricoule.user.dto.RoleDto;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface RolesService {

    RoleDto createRole(RoleDto roleDto) ;
    List<RoleDto> getAllRoles();

    void assignRolesToUser(Long userId, List<Long> roleIds);


}
