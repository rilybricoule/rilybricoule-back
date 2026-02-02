package com.sbsolutions.rilybricoule.service.service.impl;

import com.sbsolutions.rilybricoule.service.dto.ServiceDto;
import com.sbsolutions.rilybricoule.service.entity.ServiceEntity;
import com.sbsolutions.rilybricoule.service.mapper.ServiceMapper;
import com.sbsolutions.rilybricoule.service.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceServiceImplTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceServiceImpl serviceService;

    private ServiceEntity serviceEntity;
    private ServiceDto serviceDto;

    @BeforeEach
    void setUp() {
        serviceEntity = ServiceEntity.builder()
                .id(1L)
                .name("Web Design")
                .description("Professional website design")
                .price(new BigDecimal("499.99"))
                .active(true)
                .build();

        serviceDto = ServiceMapper.toDto(serviceEntity);
    }

    @Test
    void testCreateService() {
        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(serviceEntity);

        ServiceDto created = serviceService.createService(serviceDto);

        assertNotNull(created);
        assertEquals("Web Design", created.getName());
        verify(serviceRepository, times(1)).save(any(ServiceEntity.class));
    }

    @Test
    void testGetServiceById() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(serviceEntity));

        ServiceDto result = serviceService.getServiceById(1L);

        assertNotNull(result);
        assertEquals("Web Design", result.getName());
        verify(serviceRepository, times(1)).findById(1L);
    }

    @Test
    void testGetServiceById_NotFound() {
        when(serviceRepository.findById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.getServiceById(2L);
        });

        assertEquals("Service not found", exception.getMessage());
        verify(serviceRepository, times(1)).findById(2L);
    }

    @Test
    void testGetAllServices() {
        when(serviceRepository.findAll()).thenReturn(List.of(serviceEntity));

        List<ServiceDto> services = serviceService.getAllServices();

        assertNotNull(services);
        assertEquals(1, services.size());
        assertEquals("Web Design", services.get(0).getName());
        verify(serviceRepository, times(1)).findAll();
    }
    @Test
    void testUpdateService() {
        ServiceEntity updatedEntity = ServiceEntity.builder()
                .id(1L)
                .name("Web Design Pro")
                .description("Updated description")
                .price(new BigDecimal("599.99"))
                .active(true)
                .build();

        ServiceDto updatedDto = ServiceMapper.toDto(updatedEntity);

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(serviceEntity));
        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(updatedEntity);

        ServiceDto result = serviceService.updateService(updatedDto);

        assertEquals("Web Design Pro", result.getName());
        assertEquals("Updated description", result.getDescription());
        verify(serviceRepository, times(1)).findById(1L);
        verify(serviceRepository, times(1)).save(any(ServiceEntity.class));
    }

    @Test
    void testDeleteService() {
        doNothing().when(serviceRepository).deleteById(1L);

        serviceService.deleteService(1L);

        verify(serviceRepository, times(1)).deleteById(1L);
    }

}
