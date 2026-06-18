package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    List<ServiceCategory> findAllByOrderByCreatedAtDesc();

    boolean existsByNameIgnoreCase(String name);

    Optional<ServiceCategory> findByNameIgnoreCase(String name);

    long countByParentIsNotNull();
}