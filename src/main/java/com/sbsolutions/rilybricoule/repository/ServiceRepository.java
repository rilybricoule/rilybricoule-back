package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    Optional<Service> findByNameAndPrestataire_Id(String name, Long prestataireId);
    List<Service> findByPrestataire_Id(Long prestataireId);
    List<Service> findByCategory(String category);
    @Query("""
  SELECT s.prestataire.id, MIN(s.price)
  FROM Service s
  WHERE s.prestataire.id IN :ids
  GROUP BY s.prestataire.id
""")
    List<Object[]> findMinPricesByPrestataireIds(@Param("ids") List<Long> ids);
}
