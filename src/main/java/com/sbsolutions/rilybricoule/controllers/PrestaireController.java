package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.dto.PrestaireDTO;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.services.GeocodingService;
import com.sbsolutions.rilybricoule.dto.PrestataireSearchDTO;
import com.sbsolutions.rilybricoule.repository.AvisRepository;
import com.sbsolutions.rilybricoule.repository.ServiceRepository;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import com.sbsolutions.rilybricoule.repository.ServiceZoneRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/prestataires")
@RequiredArgsConstructor
public class PrestaireController {

    private final GeocodingService geocodingService;
    private final PrestaireRepository prestaireRepository;
    private final AvisRepository avisRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceZoneRepository serviceZoneRepository;

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

    @GetMapping("/nearby")
    @PreAuthorize("hasAnyRole('CLIENT', 'PRESTATAIRE', 'ADMIN')")
    public List<PrestaireDTO> getNearbyPrestataires(@RequestParam double lat, @RequestParam double lng) {
        final double RADIUS_KM = 10;
        List<Long> ids = prestaireRepository.findNearbyIds(lat, lng, RADIUS_KM);
        if (ids.isEmpty()) return List.of();
        return prestaireRepository.findAllById(ids).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "searchPrestataires",
            key =
                    "'u=' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"
                            + " + ':radius=' + #radiusKm"
                            + " + ':minRating=' + (#minRating == null ? 'null' : #minRating)"
                            + " + ':priceMin=' + (#priceMin == null ? 'null' : #priceMin)"
                            + " + ':priceMax=' + (#priceMax == null ? 'null' : #priceMax)"
                            + " + ':sortBy=' + #sortBy"
                            + " + ':order=' + #order",
            unless = "#result == null || #result.isEmpty()"
    )
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/search")
    public List<PrestataireSearchDTO> searchPrestataires(
            @RequestParam(defaultValue = "10") double radiusKm,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(defaultValue = "distance") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + email));

        if (!(user instanceof Client client)) {
            throw new IllegalArgumentException("Seul un CLIENT peut utiliser la recherche par proximité");
        }

        Double lat = client.getLatitude();
        Double lng = client.getLongitude();

        if ((lat == null || lng == null) && client.getAddress() != null && !client.getAddress().isBlank()) {
            Double[] coords = geocodingService.getCoordinates(client.getAddress());
            if (coords != null) {
                lat = coords[0];
                lng = coords[1];
                client.setLatitude(lat);
                client.setLongitude(lng);
                userRepository.save(client);
            }
        }

        if (lat == null || lng == null) {
            throw new IllegalArgumentException("Position du client indisponible (latitude/longitude null)");
        }
        final double userLat = lat;
        final double userLng = lng;
        //prestataires dans le rayon
        List<Long> ids = prestaireRepository.findNearbyIds(userLat, userLng, radiusKm);
        if (ids.isEmpty()) return List.of();

        List<Long> inZone = serviceZoneRepository.findPrestataireIdsCoveringPoint(ids, userLat, userLng);

        List<Long> noZones = serviceZoneRepository.findPrestataireIdsWithoutZones(ids);

        java.util.Set<Long> allowed = new java.util.HashSet<>(inZone);

        allowed.addAll(noZones);

        ids = ids.stream().filter(allowed::contains).toList();

        if (ids.isEmpty()) return List.of();

        List<Prestataire> prestataires = prestaireRepository.findAllById(ids);

        //AVG(rating)
        Map<Long, Double> avgRatingMap = avisRepository.findAvgRatingsByPrestataireIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> row[1] == null ? 0.0 : ((Number) row[1]).doubleValue()
                ));

        //MIN(price)
        Map<Long, BigDecimal> minPriceMap = serviceRepository.findMinPricesByPrestataireIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (BigDecimal) row[1]
                ));

        // 4) construire DTO + appliquer filtres combinables
        List<PrestataireSearchDTO> result = prestataires.stream()
                .map(p -> {
                    double distance = calculateDistanceKm(userLat, userLng, p.getLatitude(), p.getLongitude());
                    double avgRating = avgRatingMap.getOrDefault(p.getId(), 0.0);
                    BigDecimal minPrice = minPriceMap.getOrDefault(p.getId(), BigDecimal.ZERO);

                    return PrestataireSearchDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .distanceKm(distance)
                            .averageRating(avgRating)
                            .minPrice(minPrice)
                            .build();
                })
                .filter(dto -> minRating == null || dto.getAverageRating() >= minRating)
                .filter(dto -> priceMin == null || dto.getMinPrice().compareTo(priceMin) >= 0)
                .filter(dto -> priceMax == null || dto.getMinPrice().compareTo(priceMax) <= 0)
                .toList();

        //tri distance/note/prix
        if (sortBy == null || sortBy.isBlank() || "none".equalsIgnoreCase(sortBy)) {

            Map<Long, Integer> pos = new java.util.HashMap<>();
            for (int i = 0; i < ids.size(); i++) {
                pos.put(ids.get(i), i);
            }

            return result.stream()
                    .sorted(Comparator.comparingInt(p -> pos.getOrDefault(p.getId(), Integer.MAX_VALUE)))
                    .toList();
        }

        //TRI PAR NOTE
        if (sortBy.equalsIgnoreCase("note") || sortBy.equalsIgnoreCase("rating")) {
            Comparator<PrestataireSearchDTO> cmp =
                    Comparator.comparing(PrestataireSearchDTO::getAverageRating);
            if (!"asc".equalsIgnoreCase(order)) { // par défaut desc
                cmp = cmp.reversed();
            }
            return result.stream().sorted(cmp).toList();
        }
        //TRI PAR PRIX (asc/desc)
        if (sortBy.equalsIgnoreCase("prix") || sortBy.equalsIgnoreCase("price")) {

            Comparator<PrestataireSearchDTO> cmp =
                    Comparator.comparing(PrestataireSearchDTO::getMinPrice);

            if ("desc".equalsIgnoreCase(order)) {
                cmp = cmp.reversed();
            }

            return result.stream().sorted(cmp).toList();
        }
        if (sortBy.equalsIgnoreCase("distance")) {
            Comparator<PrestataireSearchDTO> cmp =
                    Comparator.comparing(PrestataireSearchDTO::getDistanceKm);

            if ("desc".equalsIgnoreCase(order)) cmp = cmp.reversed();

            return result.stream().sorted(cmp).toList();
        }
        return result;
    }

    private double calculateDistanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return Double.MAX_VALUE;

        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
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
