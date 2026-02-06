package com.sbsolutions.rilybricoule.user.controller.impl;

import com.sbsolutions.rilybricoule.user.controller.RoleRestApi;
import com.sbsolutions.rilybricoule.user.dto.RoleDto;
import com.sbsolutions.rilybricoule.user.service.RolesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoleRestController implements RoleRestApi {

    private final RolesService rolesService;

    public RoleRestController(RolesService rolesService) {
        this.rolesService = rolesService;
    }

    @Override
    public ResponseEntity<RoleDto> createRole(RoleDto roleDTO)  {
        return new ResponseEntity<>(rolesService.createRole(roleDTO), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(rolesService.getAllRoles());
    }

   @Override
    public ResponseEntity<String> assignRolesToUser(Long userId, List<Long> roleIds) {
        rolesService.assignRolesToUser(userId, roleIds);
        return ResponseEntity.ok("Roles assigned successfully");
    }

}
