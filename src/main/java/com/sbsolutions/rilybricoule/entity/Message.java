package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chat auquel le message appartient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    // Auteur du message
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column
    private LocalDateTime readAt;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(nullable = false)
    private boolean read = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✅ NEW — when message was edited
    @Column
    private LocalDateTime editedAt;

    // ✅ OPTIONAL — soft delete support
    @Column(nullable = false)
    private boolean deleted = false;

    @Column
    private LocalDateTime deletedAt;

    @Column
    private LocalDateTime archivedAt;


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
