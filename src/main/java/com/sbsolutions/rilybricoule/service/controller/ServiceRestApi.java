package com.sbsolutions.rilybricoule.service.controller;

import com.sbsolutions.rilybricoule.service.dto.ServiceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Services", description = "Services API")
@RequestMapping("/api/v1/services")
public interface ServiceRestApi {

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new service")
    @PostMapping("/add")
    ResponseEntity<ServiceDto> createService(@RequestBody ServiceDto serviceDto);

    @Operation(summary = "Update service")
    @PutMapping("/update")
    ResponseEntity<ServiceDto> updateService(@RequestBody ServiceDto serviceDto);

    @Operation(summary = "Delete service by ID")
    @DeleteMapping("/delete/{id}")
    ResponseEntity<Void> deleteService(@PathVariable Long id);

    @PreAuthorize("hasAuthority('READ_SERVICE')")
    @Operation(summary = "Get service by ID")
    @GetMapping("/{id}")
    ResponseEntity<ServiceDto> getServiceById(@PathVariable Long id);

    @PreAuthorize("hasAuthority('READ_SERVICE')")
    @Operation(summary = "Get all services")
    @GetMapping("/all")
    ResponseEntity<List<ServiceDto>> getAllServices();
}
