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
@Table(name = "clients")
public class Client extends User {

    @Column(length = 500)
    private String address;

    private Double latitude;

    private Double longitude;

    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();
}
