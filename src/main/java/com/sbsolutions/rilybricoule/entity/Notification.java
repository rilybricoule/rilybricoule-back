package com.sbsolutions.rilybricoule.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String title;

    @Column(nullable = false)
    private String contenu;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private boolean vu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String channel = "push";

    @Column(name = "triggered_by", length = 150)
    private String triggeredBy;

    @Column(name = "triggered_by_role", length = 50)
    private String triggeredByRole;

    @Column(name = "triggered_by_id")
    private Long triggeredById;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User receiver;
}