package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "prestataires")
public class Prestataire extends User {

    private String name;

    @Column(length = 1000)
    private String description;

    private String businessName;

    @Column(unique = true)
    private String cin;

    @Column(length = 500)
    private String address;

    private String zoneIntervention;

    private String languages;

    private Integer experienceYears;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProviderStatus status = ProviderStatus.PENDING;

    @Column(length = 1000)
    private String adminComment;

    public enum ProviderStatus {
        PENDING,
        APPROVED,
        REJECTED,
        SUSPENDED
    }



    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Service> services = new HashSet<>();

    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Avis> avis = new HashSet<>();

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private boolean verified = false;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean available = true;
}
