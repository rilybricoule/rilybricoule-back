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

    @Builder.Default
    @Column(nullable = false)
    private boolean verified = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean available = true;

    @Builder.Default
    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<Service> services = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<Avis> avis = new HashSet<>();
}
