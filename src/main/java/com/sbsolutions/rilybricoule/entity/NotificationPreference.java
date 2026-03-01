package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean messageEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean reservationEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean paiementEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean avisEnabled = true;
}