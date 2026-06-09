package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservation_dispatches",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"reservation_id", "prestataire_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestataire_id", nullable = false)
    private Prestataire prestataire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DispatchStatus status = DispatchStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column
    private LocalDateTime respondedAt;
}