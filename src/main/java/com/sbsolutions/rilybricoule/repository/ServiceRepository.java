package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    Optional<Service> findByNameAndPrestaireId(String name, Long prestaireId);
    List<Service> findByPrestaireId(Long prestaireId);
    List<Service> findByCategory(String category);
}
