package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.admin.AdminCategoryDTO;
import com.sbsolutions.rilybricoule.dto.admin.AdminCategoryStatsDTO;
import com.sbsolutions.rilybricoule.entity.ServiceCategory;
import com.sbsolutions.rilybricoule.repository.ServiceCategoryRepository;
import com.sbsolutions.rilybricoule.repository.ServiceRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.sbsolutions.rilybricoule.entity.Service;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final ServiceCategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<AdminCategoryDTO> getAll() {
        return categoryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }


    private List<String> getCategoryAndChildrenNames(ServiceCategory category) {
        List<String> names = new java.util.ArrayList<>();
        collectCategoryNames(category, names);
        return names;
    }

    private void collectCategoryNames(ServiceCategory category, List<String> names) {
        names.add(category.getName());

        if (category.getChildren() != null) {
            for (ServiceCategory child : category.getChildren()) {
                collectCategoryNames(child, names);
            }
        }
    }

    @Transactional
    public AdminCategoryDTO create(CategoryRequest request) {
        String name = cleanName(request.getName());

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        }

        ServiceCategory parent = resolveParent(request.getParentId());

        ServiceCategory category = ServiceCategory.builder()
                .parent(parent)
                .name(name)
                .description(request.getDescription())
                .icon(request.getIcon())
                .attributesJson(request.getAttributesJson())
                .active(request.getActive() == null || request.getActive())
                .build();

        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public AdminCategoryDTO update(Long id, CategoryRequest request) {
        ServiceCategory category = findCategory(id);
        String name = cleanName(request.getName());

        categoryRepository.findByNameIgnoreCase(name)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
                    }
                });

        ServiceCategory parent = resolveParent(request.getParentId());

        if (parent != null && parent.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category cannot be its own parent");
        }

        category.setParent(parent);
        category.setName(name);
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setAttributesJson(request.getAttributesJson());

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        return toDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public AdminCategoryStatsDTO getStats() {
        return AdminCategoryStatsDTO.builder()
                .totalCategories(categoryRepository.count())
                .totalSubCategories(categoryRepository.countByParentIsNotNull())
                .totalServices(
                        serviceRepository.countByModerationStatus(
                                Service.ModerationStatus.APPROVED
                        )
                )
                .totalProviders(
                        serviceRepository.countDistinctProvidersByModerationStatus(
                                Service.ModerationStatus.APPROVED
                        )
                )
                .build();
    }

    @Transactional
    public void delete(Long id) {
        ServiceCategory category = findCategory(id);
        categoryRepository.delete(category);
    }

    private ServiceCategory findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private ServiceCategory resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }

        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent category not found"));
    }

    private String cleanName(String name) {
        if (name == null || name.trim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }

        return name.trim();
    }

    private AdminCategoryDTO toDto(ServiceCategory category) {
        String categoryName = category.getName();
        List<String> categoryNames = getCategoryAndChildrenNames(category);


        return AdminCategoryDTO.builder()
                .id(category.getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .name(category.getName())
                .description(category.getDescription())
                .icon(category.getIcon())
                .attributesJson(category.getAttributesJson())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .serviceCount(
                        serviceRepository.countByCategoryInAndModerationStatus(
                                categoryNames,
                                Service.ModerationStatus.APPROVED
                        )
                )
                .providerCount(
                        serviceRepository.countDistinctProvidersByCategoryInAndModerationStatus(
                                categoryNames,
                                Service.ModerationStatus.APPROVED
                        )
                )
                .build();
    }

    @Data
    public static class CategoryRequest {
        private Long parentId;
        private String name;
        private String description;
        private String icon;
        private String attributesJson;
        private Boolean active;
    }
}