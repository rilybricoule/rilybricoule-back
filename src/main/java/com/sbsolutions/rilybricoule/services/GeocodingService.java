package com.sbsolutions.rilybricoule.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.annotation.Cacheable;

import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    @Value("${google.maps.api.key:}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    @Cacheable(
            value = "geocode",
            key = "#address == null ? 'null' : #address.trim().toLowerCase()",
            unless = "#result == null"
    )
    public Double[] getCoordinates(String address) {

        if (address == null || address.isBlank()) {
            return null;
        }
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }

        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = "https://maps.googleapis.com/maps/api/geocode/json" +
                "?address=" + encodedAddress +
                "&key=" + apiKey;

        Map response = restTemplate.getForObject(url, Map.class);

        if (response == null || !"OK".equals(response.get("status"))) {
            return null;
        }

        var results = (java.util.List<Map>) response.get("results");
        if (results == null || results.isEmpty()) {
            return null;
        }
        var geometry = (Map) results.get(0).get("geometry");
        var location = (Map) geometry.get("location");

        Double lat = ((Number) location.get("lat")).doubleValue();
        Double lng = ((Number) location.get("lng")).doubleValue();

        return new Double[]{lat, lng};
    }
}
