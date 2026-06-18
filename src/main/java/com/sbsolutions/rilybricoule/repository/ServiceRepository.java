package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
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

    List<Service> findAllByOrderByCreatedAtDesc();

    long countByCategory(String category);

    @Query("select count(distinct s.prestataire.id) from Service s where s.category = :category")
    long countDistinctProvidersByCategory(@Param("category") String category);

    @Query("select count(distinct s.prestataire.id) from Service s")
    long countDistinctProviders();

    long countByCategoryIn(Collection<String> categories);

    long countByCategoryInAndModerationStatus(
            Collection<String> categories,
            Service.ModerationStatus moderationStatus
    );

    @Query("""
    select count(distinct s.prestataire.id)
    from Service s
    where s.category in :categories
      and s.moderationStatus = :status
""")
    long countDistinctProvidersByCategoryInAndModerationStatus(
            @Param("categories") Collection<String> categories,
            @Param("status") Service.ModerationStatus status
    );

    List<Service> findByCategoryInAndModerationStatus(
            Collection<String> categories,
            Service.ModerationStatus moderationStatus
    );

    long countByModerationStatus(Service.ModerationStatus moderationStatus);

    @Query("""
    select count(distinct s.prestataire.id)
    from Service s
    where s.moderationStatus = :status
""")
    long countDistinctProvidersByModerationStatus(
            @Param("status") Service.ModerationStatus status
    );
}
