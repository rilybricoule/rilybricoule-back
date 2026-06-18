package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false)
    @Builder.Default
    private boolean litige = false;

    @Column(nullable = false, length = 50)
    private String fromType;

    @Column( length = 50)
    private String fromRole;


    @Column(length = 50)
    private String fromId;

    @Column(nullable = false, length = 255)
    private String fromName;

    @Column(length = 50)
    private String reservationId;

    @Column(length = 255)
    private String reservationTitle;

    @Column(length = 1000)
    private String adminNote;

    @Column(length = 1000)
    private String resolutionNote;

    @Column(length = 100)
    private String resolutionAction;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<SupportTicketMessage> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
