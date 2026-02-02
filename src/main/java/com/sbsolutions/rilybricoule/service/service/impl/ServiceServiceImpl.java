package com.sbsolutions.rilybricoule.service.service.impl;

import com.sbsolutions.rilybricoule.service.dto.ServiceDto;
import com.sbsolutions.rilybricoule.service.entity.ServiceEntity;
import com.sbsolutions.rilybricoule.service.mapper.ServiceMapper;
import com.sbsolutions.rilybricoule.service.repository.ServiceRepository;
import com.sbsolutions.rilybricoule.service.service.ServiceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public ServiceDto createService(ServiceDto serviceDto) {
        ServiceEntity entity = ServiceMapper.toEntity(serviceDto);
        entity = serviceRepository.save(entity);
        return ServiceMapper.toDto(entity);
    }

    @Override
    public ServiceDto updateService(ServiceDto serviceDto) {
        ServiceEntity entity = serviceRepository.findById(serviceDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        entity.setName(serviceDto.getName());
        entity.setDescription(serviceDto.getDescription());
        entity.setPrice(serviceDto.getPrice());
        entity.setActive(serviceDto.getActive());

        entity = serviceRepository.save(entity);
        return ServiceMapper.toDto(entity);
    }

    @Override
    public void deleteService(Long serviceId) {
        serviceRepository.deleteById(serviceId);
    }

    @Override
    public ServiceDto getServiceById(Long serviceId) {
        ServiceEntity entity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        return ServiceMapper.toDto(entity);
    }

    @Override
    public List<ServiceDto> getAllServices() {
        return serviceRepository.findAll()
                .stream()
                .map(ServiceMapper::toDto)
                .toList();
    }
}
