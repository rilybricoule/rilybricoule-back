package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCategoryDTO {

    private Long id;
    private Long parentId;

    private String name;
    private String description;
    private String icon;
    private String attributesJson;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private long serviceCount;
    private long providerCount;
}
