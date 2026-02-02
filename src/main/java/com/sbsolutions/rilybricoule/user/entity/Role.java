package com.sbsolutions.rilybricoule.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(updatable = false)
    private String createdBy;

    @Column(updatable = false)
    private LocalDateTime createdDate;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Permission> permissions = new HashSet<>();
}
