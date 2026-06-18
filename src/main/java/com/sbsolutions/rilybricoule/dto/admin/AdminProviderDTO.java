package com.sbsolutions.rilybricoule.dto.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProviderDTO {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private String city;

    private String name;

    private String businessName;

    private String description;

    private String address;

    private Boolean enabled;

    private String status;

    private Boolean verified;

    private Boolean available;

    private Boolean active;

    private String adminComment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long completedInterventions;

    private Double averageRating;
}