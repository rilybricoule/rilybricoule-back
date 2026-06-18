package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSettings {

    @Id
    private Long id;

    @Column(nullable = false)
    private int globalRate;

    @Column(nullable = false)
    private boolean usePerCategory;

    @Column(columnDefinition = "TEXT")
    private String categoriesJson;

    @Column(columnDefinition = "TEXT")
    private String gatewaysJson;

    @Column(nullable = false, length = 20)
    private String currency;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(nullable = false, length = 80)
    private String timezone;

    @Column(nullable = false, length = 30)
    private String dateFormat;

    @Column(nullable = false)
    private int cancelWindow;

    @Column(nullable = false)
    private int cancelFeePercent;

    @Column(nullable = false)
    private int freeCancelWindow;

    @Column(nullable = false)
    private boolean autoRefund;

    @Column(nullable = false, length = 20)
    private String refundDelay;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
