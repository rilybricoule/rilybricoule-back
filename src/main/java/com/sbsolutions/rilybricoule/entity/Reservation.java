package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate reservationDate;
    
    @Column(nullable = false)
    private LocalTime reservationTime;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING_PAYMENT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestataire_id", nullable = false)
    private Prestataire prestataire;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = true)
    private Coupon coupon;
    
    @OneToOne(mappedBy = "reservation", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = true)
    private Avis avis;
    
    @OneToOne(mappedBy = "reservation", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = true)
    private Paiement paiement;

    @Column
    private LocalDateTime cancelledAt;
    
    public enum ReservationStatus {
        PENDING_PAYMENT, CONFIRMED, COMPLETED, CANCELLED
    }
}
