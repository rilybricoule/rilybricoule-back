package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.ProviderDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderDocumentRepository extends JpaRepository<ProviderDocument, Long> {
    List<ProviderDocument> findByPrestataire_IdOrderByCreatedAtDesc(Long prestataireId);
}