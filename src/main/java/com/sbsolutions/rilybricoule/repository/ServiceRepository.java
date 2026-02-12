package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    Optional<Service> findByNameAndPrestataireId(String name, Long prestataireId);
    List<Service> findByPrestataireId(Long prestataireId);
    List<Service> findByCategory(String category);
}
