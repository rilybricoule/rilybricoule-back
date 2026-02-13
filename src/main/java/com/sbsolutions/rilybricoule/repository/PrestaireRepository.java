package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Prestataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrestaireRepository extends JpaRepository<Prestataire, Long> {
    Optional<Prestataire> findByEmail(String email);
    Optional<Prestataire> findByName(String name);

    @Query(value = """
    SELECT p.id
    FROM prestataires p
    WHERE p.latitude IS NOT NULL
      AND p.longitude IS NOT NULL
      AND (
        6371 * 2 * ASIN(
          SQRT(
            POWER(SIN(RADIANS((p.latitude - :lat) / 2)), 2) +
            COS(RADIANS(:lat)) * COS(RADIANS(p.latitude)) *
            POWER(SIN(RADIANS((p.longitude - :lng) / 2)), 2)
          )
        )
      ) <= :radiusKm
    ORDER BY (
        6371 * 2 * ASIN(
          SQRT(
            POWER(SIN(RADIANS((p.latitude - :lat) / 2)), 2) +
            COS(RADIANS(:lat)) * COS(RADIANS(p.latitude)) *
            POWER(SIN(RADIANS((p.longitude - :lng) / 2)), 2)
          )
        )
    ) ASC
    """, nativeQuery = true)
    List<Long> findNearbyIds(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm
    );

}
