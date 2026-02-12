package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String firstName;
    
    @Column(nullable = false, length = 255)
    private String lastName;
    
    @Column(nullable = false, length = 255, unique = true)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 500)
    private String address;

    @ManyToOne
    private Client client;

    @Column
    private Double latitude;

    @Column
    private Double longitude;
    

    @OneToMany(mappedBy = "client", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Reservation> reservations = new HashSet<>();
}
