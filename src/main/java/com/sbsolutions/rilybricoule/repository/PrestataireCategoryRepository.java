package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.PrestataireCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrestataireCategoryRepository extends JpaRepository<PrestataireCategory, Long> {

    @Query("""
    SELECT DISTINCT pc.prestataire.id
    FROM PrestataireCategory pc
    WHERE pc.prestataire.id IN :ids
      AND (:category IS NULL OR LOWER(pc.category) = LOWER(:category))
""")
    List<Long> findPrestataireIdsByCategory(
            @Param("ids") List<Long> ids,
            @Param("category") String category
    );

}