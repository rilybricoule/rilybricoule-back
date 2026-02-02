package com.sbsolutions.rilybricoule.service.controller.impl;

import com.sbsolutions.rilybricoule.service.controller.ServiceRestApi;
import com.sbsolutions.rilybricoule.service.dto.ServiceDto;
import com.sbsolutions.rilybricoule.service.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ServiceRestController implements ServiceRestApi {

    private final ServiceService serviceService;

    public ServiceRestController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @Override
    public ResponseEntity<ServiceDto> createService(ServiceDto serviceDto) {
        return ResponseEntity.ok(serviceService.createService(serviceDto));
    }

    @Override
    public ResponseEntity<ServiceDto> updateService(ServiceDto serviceDto) {
        return ResponseEntity.ok(serviceService.updateService(serviceDto));
    }

    @Override
    public ResponseEntity<Void> deleteService(Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ServiceDto> getServiceById(Long id) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }

    @Override
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        List<ServiceDto> services = serviceService.getAllServices();
        return ResponseEntity.ok(services);
    }
}
