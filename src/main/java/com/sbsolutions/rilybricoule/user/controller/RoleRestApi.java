package com.sbsolutions.rilybricoule.user.controller;

import com.sbsolutions.rilybricoule.user.dto.RoleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Roles", description = "Roles Api")
@RequestMapping(path = "/api/v1/roles")
public interface RoleRestApi {

    @Operation(summary = "Create new role",
            description = "Create new role", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    @PostMapping("/addRole")
    ResponseEntity<RoleDto> createRole(@RequestBody RoleDto roleDTO);


    @Operation(summary = "Get all roles", description = "Fetch all roles")
    @GetMapping("/all")
    ResponseEntity<List<RoleDto>> getAllRoles();

    @PutMapping("/assignRolesToUser")
    public ResponseEntity<String> assignRolesToUser(@RequestParam Long userId, @RequestParam List<Long> roleIds) ;


}
