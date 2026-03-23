package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.PrestataireSearchDTO;
import com.sbsolutions.rilybricoule.entity.Client;
import com.sbsolutions.rilybricoule.entity.Prestataire;
import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.repository.AvisRepository;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.repository.ServiceRepository;
import com.sbsolutions.rilybricoule.repository.ServiceZoneRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import com.sbsolutions.rilybricoule.repository.ReservationRepository;
import com.sbsolutions.rilybricoule.repository.PrestataireCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrestataireSearchService {

    private final GeocodingService geocodingService;
    private final PrestaireRepository prestaireRepository;
    private final AvisRepository avisRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceZoneRepository serviceZoneRepository;
    private final ReservationRepository reservationRepository;
    private final PrestataireCategoryRepository prestataireCategoryRepository;



    public List<PrestataireSearchDTO> search(
            double radiusKm,
            Double minRating,
            BigDecimal priceMin,
            BigDecimal priceMax,
            Boolean available,
            String category,
            String subCategory,
            String sortBy,
            String order
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

        List<Long> ids = prestaireRepository.findNearbyIds(userLat, userLng, radiusKm);
        if (ids.isEmpty()) return List.of();

        List<Long> inZone = serviceZoneRepository.findPrestataireIdsCoveringPoint(ids, userLat, userLng);
        List<Long> noZones = serviceZoneRepository.findPrestataireIdsWithoutZones(ids);

        java.util.Set<Long> inZoneSet = new java.util.HashSet<>(inZone);
        java.util.Set<Long> noZoneSet = new java.util.HashSet<>(noZones);

        if (category != null) {

            List<Long> matchingIds = prestataireCategoryRepository
                    .findPrestataireIdsByCategory(ids, category);

            ids = ids.stream().filter(matchingIds::contains).toList();
            if (ids.isEmpty()) return List.of();
        }

        List<Prestataire> prestataires = prestaireRepository.findAllById(ids);
        Map<Long, Long> activeJobsMap = reservationRepository.countActiveReservationsByPrestataireIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
        Map<Long, Long> completedJobsMap = reservationRepository.countCompletedReservationsByPrestataireIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        Map<Long, Double> avgRatingMap = avisRepository.findAvgRatingsByPrestataireIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> row[1] == null ? 0.0 : ((Number) row[1]).doubleValue()
                ));

        Map<Long, BigDecimal> minPriceMap = serviceRepository.findMinPricesByPrestataireIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (BigDecimal) row[1]
                ));

        List<PrestataireSearchDTO> result = prestataires.stream()
                .filter(p -> available == null || p.isAvailable() == available)
                .map(p -> {
                    double distance = calculateDistanceKm(userLat, userLng, p.getLatitude(), p.getLongitude());
                    double avgRating = avgRatingMap.getOrDefault(p.getId(), 0.0);
                    BigDecimal minPrice = minPriceMap.getOrDefault(p.getId(), BigDecimal.ZERO);
                    boolean isAvailable = p.isAvailable();
                    long activeJobs = activeJobsMap.getOrDefault(p.getId(), 0L);
                    long completedJobs = completedJobsMap.getOrDefault(p.getId(), 0L);
                    boolean isInZone = inZoneSet.contains(p.getId());
                    boolean hasNoZoneConfigured = noZoneSet.contains(p.getId());
                    double score = calculateScore(avgRating, distance, activeJobs, isAvailable,completedJobs, isInZone, hasNoZoneConfigured);

                    return PrestataireSearchDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .distanceKm(distance)
                            .score(score)
                            .averageRating(avgRating)
                            .minPrice(minPrice)
                            .build();
                })
                .filter(dto -> minRating == null || dto.getAverageRating() >= minRating)
                .filter(dto -> priceMin == null || dto.getMinPrice().compareTo(priceMin) >= 0)
                .filter(dto -> priceMax == null || dto.getMinPrice().compareTo(priceMax) <= 0)
                .toList();


        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "score";
        }

        if (order == null || order.isBlank()) {
            if ("distance".equalsIgnoreCase(sortBy) || "price".equalsIgnoreCase(sortBy) || "prix".equalsIgnoreCase(sortBy)) {
                order = "asc";
            } else {
                order = "desc";
            }
        }

        if ("score".equalsIgnoreCase(sortBy) || "none".equalsIgnoreCase(sortBy)) {
            Comparator<PrestataireSearchDTO> cmp =
                    Comparator.comparing(PrestataireSearchDTO::getScore);

            if (!"asc".equalsIgnoreCase(order)) {
                cmp = cmp.reversed();
            }

            return result.stream().sorted(cmp).toList();
        }

        if ("note".equalsIgnoreCase(sortBy) || "rating".equalsIgnoreCase(sortBy)) {
            Comparator<PrestataireSearchDTO> cmp =
                    Comparator.comparing(PrestataireSearchDTO::getAverageRating);

            if (!"asc".equalsIgnoreCase(order)) {
                cmp = cmp.reversed();
            }

            return result.stream().sorted(cmp).toList();
        }

        if ("prix".equalsIgnoreCase(sortBy) || "price".equalsIgnoreCase(sortBy)) {
            Comparator<PrestataireSearchDTO> cmp =
                    Comparator.comparing(PrestataireSearchDTO::getMinPrice);

            if ("desc".equalsIgnoreCase(order)) {
                cmp = cmp.reversed();
            }

            return result.stream().sorted(cmp).toList();
        }

        if ("distance".equalsIgnoreCase(sortBy)) {
            Comparator<PrestataireSearchDTO> cmp =
                    Comparator.comparing(PrestataireSearchDTO::getDistanceKm);

            if ("desc".equalsIgnoreCase(order)) {
                cmp = cmp.reversed();
            }

            return result.stream().sorted(cmp).toList();
        }

        return result.stream()
                .sorted(Comparator.comparing(PrestataireSearchDTO::getScore).reversed())
                .toList();
    }

    private double calculateScore(
            double rating,
            double distanceKm,
            long activeJobs,
            boolean available,
            long completedJobs,
            boolean inZone,
            boolean noZoneConfigured) {

        double availabilityBonus = available? 15:-15;
        double zoneBonus;
        if (inZone) {
            zoneBonus = 20;
        } else if (noZoneConfigured) {
            zoneBonus = 0;
        } else {
            zoneBonus = -30;
        }
        double reliabilityBonus = Math.min(completedJobs * 2, 20);

        return (rating * 10) - distanceKm - (activeJobs * 5) + availabilityBonus + zoneBonus + reliabilityBonus;
    }

    private double calculateDistanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    //search for dispatch
    public List<PrestataireSearchDTO> searchForDispatch(
            Client client,
            double radiusKm,
            String category,
            String subCategory
    ) {
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

        List<Long> ids = prestaireRepository.findNearbyIds(userLat, userLng, radiusKm);
        if (ids.isEmpty()) return List.of();

        List<Long> inZone = serviceZoneRepository.findPrestataireIdsCoveringPoint(ids, userLat, userLng);
        List<Long> noZones = serviceZoneRepository.findPrestataireIdsWithoutZones(ids);

        java.util.Set<Long> inZoneSet = new java.util.HashSet<>(inZone);
        java.util.Set<Long> noZoneSet = new java.util.HashSet<>(noZones);

        if (category != null) {
            List<Long> matchingIds = prestataireCategoryRepository
                    .findPrestataireIdsByCategory(ids, category);

            ids = ids.stream()
                    .filter(matchingIds::contains)
                    .toList();

            if (ids.isEmpty()) return List.of();
        }

        List<Prestataire> prestataires = prestaireRepository.findAllById(ids);

        Map<Long, Long> activeJobsMap = reservationRepository.countActiveReservationsByPrestataireIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        Map<Long, Long> completedJobsMap = reservationRepository.countCompletedReservationsByPrestataireIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        Map<Long, Double> avgRatingMap = avisRepository.findAvgRatingsByPrestataireIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> row[1] == null ? 0.0 : ((Number) row[1]).doubleValue()
                ));

        Map<Long, BigDecimal> minPriceMap = serviceRepository.findMinPricesByPrestataireIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (BigDecimal) row[1]
                ));

        return prestataires.stream()
                .map(p -> {
                    double distance = calculateDistanceKm(userLat, userLng, p.getLatitude(), p.getLongitude());
                    double avgRating = avgRatingMap.getOrDefault(p.getId(), 0.0);
                    BigDecimal minPrice = minPriceMap.getOrDefault(p.getId(), BigDecimal.ZERO);
                    boolean isAvailable = p.isAvailable();
                    long activeJobs = activeJobsMap.getOrDefault(p.getId(), 0L);
                    long completedJobs = completedJobsMap.getOrDefault(p.getId(), 0L);
                    boolean isInZone = inZoneSet.contains(p.getId());
                    boolean hasNoZoneConfigured = noZoneSet.contains(p.getId());

                    double score = calculateScore(
                            avgRating,
                            distance,
                            activeJobs,
                            isAvailable,
                            completedJobs,
                            isInZone,
                            hasNoZoneConfigured
                    );

                    return PrestataireSearchDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .distanceKm(distance)
                            .score(score)
                            .averageRating(avgRating)
                            .minPrice(minPrice)
                            .build();
                })
                .sorted(Comparator.comparing(PrestataireSearchDTO::getScore).reversed())
                .toList();
    }

}