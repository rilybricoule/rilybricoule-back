package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Prestataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrestaireRepository extends JpaRepository<Prestataire, Long> {
    Optional<Prestataire> findByEmail(String email);
    Optional<Prestataire> findByName(String name);
}
