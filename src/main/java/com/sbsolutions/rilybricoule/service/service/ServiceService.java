package com.sbsolutions.rilybricoule.service.service;

import com.sbsolutions.rilybricoule.service.dto.ServiceDto;

import java.util.List;

public interface ServiceService {

    ServiceDto createService(ServiceDto serviceDto);

    ServiceDto updateService(ServiceDto serviceDto);

    void deleteService(Long serviceId);

    ServiceDto getServiceById(Long serviceId);

    List<ServiceDto> getAllServices();
}
