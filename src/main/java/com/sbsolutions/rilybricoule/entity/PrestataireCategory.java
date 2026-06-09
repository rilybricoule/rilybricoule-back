package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prestataire_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestataireCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestataire_id", nullable = false)
    private Prestataire prestataire;
}