package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {
    Optional<Avis> findByReservationId(Long reservationId);

    List<Avis> findByPrestataireId(Long prestataireId);

    List<Avis> findByPrestataireIdOrderByCreatedDateDesc(Long prestataireId);

    @Query("""
  SELECT a.prestataire.id, AVG(a.rating)
  FROM Avis a
  WHERE a.prestataire.id IN :ids
  GROUP BY a.prestataire.id
""")
    List<Object[]> findAvgRatingsByPrestataireIds(@Param("ids") List<Long> ids);


    @Query("select coalesce(avg(a.rating), 0) from Avis a where a.prestataire.id = :prestataireId")
    Double findAverageRatingByPrestataireId(@Param("prestataireId") Long prestataireId);
}