package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.PrestaireDTO;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.services.GeocodingService;
import com.sbsolutions.rilybricoule.dto.PrestataireSearchDTO;
import com.sbsolutions.rilybricoule.services.PrestataireSearchService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prestataires")
@RequiredArgsConstructor
public class PrestaireController {

    private final GeocodingService geocodingService;
    private final PrestaireRepository prestaireRepository;
    private final PrestataireSearchService prestataireSearchService;

    @CacheEvict(value = "searchPrestataires", allEntries = true)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrestaireDTO> createPrestataire(@RequestBody PrestaireDTO request) {
        Prestataire prestataire = Prestataire.builder()
            .name(request.getName())
            .description(request.getDescription())
            .phone(request.getPhone())
            .email(request.getEmail())
            .address(request.getAddress())
            .build();
        setLatLngIfPossible(prestataire);
        Prestataire saved = prestaireRepository.save(prestataire);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }
    @GetMapping("/me")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public ResponseEntity<PrestaireDTO> me() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Prestataire prestataire = prestaireRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Prestataire introuvable: " + email));

        return ResponseEntity.ok(toDTO(prestataire));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESTATAIRE', 'CLIENT', 'ADMIN')")
    public ResponseEntity<?> getPrestataire(@PathVariable Long id) {
        Optional<Prestataire> prestataire = prestaireRepository.findById(id);
        if (prestataire.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Prestataire not found");
        }
        return ResponseEntity.ok(toDTO(prestataire.get()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PRESTATAIRE', 'CLIENT', 'ADMIN')")
    public ResponseEntity<List<PrestaireDTO>> getAllPrestataires() {
        List<PrestaireDTO> prestataires = prestaireRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(prestataires);
    }

    @CacheEvict(value = "searchPrestataires", allEntries = true)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESTATAIRE', 'ADMIN')")
    public ResponseEntity<?> updatePrestataire(@PathVariable Long id, @RequestBody PrestaireDTO request) {
        Optional<Prestataire> prestaireOpt = prestaireRepository.findById(id);
        if (prestaireOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Prestataire not found");
        }

        Prestataire prestataire = prestaireOpt.get();
        prestataire.setName(request.getName());
        prestataire.setDescription(request.getDescription());
        prestataire.setPhone(request.getPhone());
        prestataire.setEmail(request.getEmail());
        prestataire.setAddress(request.getAddress());
        setLatLngIfPossible(prestataire);
        Prestataire updated = prestaireRepository.save(prestataire);
        return ResponseEntity.ok(toDTO(updated));
    }

    @CacheEvict(value = "searchPrestataires", allEntries = true)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePrestataire(@PathVariable Long id) {
        if (!prestaireRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Prestataire not found");
        }
        prestaireRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void setLatLngIfPossible(Prestataire prestataire) {
        if (prestataire.getAddress() != null && !prestataire.getAddress().isBlank()) {
            Double[] coords = geocodingService.getCoordinates(prestataire.getAddress());
            if (coords != null) {
                prestataire.setLatitude(coords[0]);
                prestataire.setLongitude(coords[1]);
            }
        }
    }

    @Cacheable(
            value = "searchPrestataires",
            key =
                    "'u=' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"
                            + " + ':radius=' + #radiusKm"
                            + " + ':minRating=' + (#minRating == null ? 'null' : #minRating)"
                            + " + ':priceMin=' + (#priceMin == null ? 'null' : #priceMin)"
                            + " + ':priceMax=' + (#priceMax == null ? 'null' : #priceMax)"
                            + " + ':available=' + (#available == null ? 'null' : #available)"
                            + " + ':category=' + (#category == null ? 'null' : #category)"
                            + " + ':subCategory=' + (#subCategory == null ? 'null' : #subCategory)"
                            + " + ':sortBy=' + #sortBy"
                            + " + ':order=' + #order",
            unless = "#result == null || #result.isEmpty()"
    )
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/search")
    public List<PrestataireSearchDTO> searchPrestataires(
            @RequestParam(defaultValue = "20") double radiusKm,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(defaultValue = "score") String sortBy,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String order
    ) {
        return prestataireSearchService.search(radiusKm,minRating,priceMin,priceMax,available,category,
                subCategory,sortBy,order);
    }

    private PrestaireDTO toDTO(Prestataire prestataire) {
        return PrestaireDTO.builder()
            .id(prestataire.getId())
            .name(prestataire.getName())
            .description(prestataire.getDescription())
            .phone(prestataire.getPhone())
            .email(prestataire.getEmail())
            .address(prestataire.getAddress())
            .latitude(prestataire.getLatitude())
            .longitude(prestataire.getLongitude())
            .build();
    }
}
