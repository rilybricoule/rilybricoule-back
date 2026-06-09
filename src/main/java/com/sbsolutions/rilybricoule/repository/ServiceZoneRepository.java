package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.ServiceZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceZoneRepository extends JpaRepository<ServiceZone, Long> {

    List<ServiceZone> findByPrestataireId(Long prestataireId);

    @Query(value = """
        SELECT DISTINCT sz.prestataire_id
        FROM service_zones sz
        WHERE sz.prestataire_id IN (:prestIds)
          AND ST_DWithin(
                sz.center_geog,
                CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography),
                sz.radius_meters
          )
        """, nativeQuery = true)
    List<Long> findPrestataireIdsCoveringPoint(
            @Param("prestIds") List<Long> prestIds,
            @Param("lat") double lat,
            @Param("lng") double lng
    );

    @Query(value = """
        SELECT p.id
        FROM prestataires p
        WHERE p.id IN (:prestIds)
          AND NOT EXISTS (
              SELECT 1 FROM service_zones sz WHERE sz.prestataire_id = p.id
          )
        """, nativeQuery = true)
    List<Long> findPrestataireIdsWithoutZones(@Param("prestIds") List<Long> prestIds);
}