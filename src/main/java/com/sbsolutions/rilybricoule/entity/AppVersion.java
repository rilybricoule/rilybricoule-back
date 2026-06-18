package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private String version;

    @Column(name = "build_number", nullable = false)
    private String buildNumber;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(nullable = false)
    private String status;

    @Column(name = "force_update", nullable = false)
    private boolean forceUpdate;

    @Column(name = "min_supported_version")
    private String minSupportedVersion;

    @Column(columnDefinition = "TEXT")
    private String changelog;

    @Column(columnDefinition = "TEXT")
    private String note;
}