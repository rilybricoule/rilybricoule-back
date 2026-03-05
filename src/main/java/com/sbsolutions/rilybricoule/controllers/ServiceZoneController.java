package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.ServiceZoneRequest;
import com.sbsolutions.rilybricoule.dto.ServiceZoneResponse;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.ServiceZone;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ServiceZoneRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestataires/me/zones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESTATAIRE')")
public class ServiceZoneController {

    private final PrestaireRepository prestaireRepository;
    private final ServiceZoneRepository serviceZoneRepository;

    private Prestataire currentPrestataire() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return prestaireRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Prestataire introuvable: " + email));
    }

    @GetMapping
    public List<ServiceZoneResponse> list() {
        Prestataire p = currentPrestataire();
        return serviceZoneRepository.findByPrestataireId(p.getId())
                .stream().map(this::toResp).toList();
    }

    @CacheEvict(value = "searchPrestataires", allEntries = true)
    @PostMapping
    public ResponseEntity<ServiceZoneResponse> create(@Valid @RequestBody ServiceZoneRequest req) {
        Prestataire p = currentPrestataire();

        ServiceZone zone = ServiceZone.builder()
                .prestataire(p)
                .centerLat(req.getCenterLat())
                .centerLng(req.getCenterLng())
                .radiusMeters(req.getRadiusMeters())
                .label(req.getLabel())
                .build();

        ServiceZone saved = serviceZoneRepository.save(zone);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResp(saved));
    }

    @CacheEvict(value = "searchPrestataires", allEntries = true)
    @PutMapping("/{zoneId}")
    public ServiceZoneResponse update(@PathVariable Long zoneId, @Valid @RequestBody ServiceZoneRequest req) {
        Prestataire p = currentPrestataire();

        ServiceZone zone = serviceZoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Zone introuvable"));

        if (!zone.getPrestataire().getId().equals(p.getId())) {
            throw new IllegalArgumentException("Accès interdit à cette zone");
        }

        zone.setCenterLat(req.getCenterLat());
        zone.setCenterLng(req.getCenterLng());
        zone.setRadiusMeters(req.getRadiusMeters());
        zone.setLabel(req.getLabel());

        return toResp(serviceZoneRepository.save(zone));
    }

    @CacheEvict(value = "searchPrestataires", allEntries = true)
    @DeleteMapping("/{zoneId}")
    public ResponseEntity<Void> delete(@PathVariable Long zoneId) {
        Prestataire p = currentPrestataire();

        ServiceZone zone = serviceZoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Zone introuvable"));

        if (!zone.getPrestataire().getId().equals(p.getId())) {
            throw new IllegalArgumentException("Accès interdit à cette zone");
        }

        serviceZoneRepository.delete(zone);
        return ResponseEntity.noContent().build();
    }

    private ServiceZoneResponse toResp(ServiceZone z) {
        return ServiceZoneResponse.builder()
                .id(z.getId())
                .centerLat(z.getCenterLat())
                .centerLng(z.getCenterLng())
                .radiusMeters(z.getRadiusMeters())
                .label(z.getLabel())
                .build();
    }
}