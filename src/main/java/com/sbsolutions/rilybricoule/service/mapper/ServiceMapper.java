package com.sbsolutions.rilybricoule.service.mapper;

import com.sbsolutions.rilybricoule.service.dto.ServiceDto;
import com.sbsolutions.rilybricoule.service.entity.ServiceEntity;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public static ServiceDto toDto(ServiceEntity entity) {
        if (entity == null) return null;

        return ServiceDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .active(entity.getActive())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .build();
    }

    public static ServiceEntity toEntity(ServiceDto dto) {
        if (dto == null) return null;

        return ServiceEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .active(dto.getActive())
                .createdBy(dto.getCreatedBy())
                .createdDate(dto.getCreatedDate())
                .build();
    }
}
