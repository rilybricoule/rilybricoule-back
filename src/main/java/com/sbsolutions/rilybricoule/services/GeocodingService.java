package com.sbsolutions.rilybricoule.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
    public Double[] getCoordinates(String address) {

        System.out.println("GOOGLE MAPS KEY LOADED? " + (apiKey != null && !apiKey.isBlank()));

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("Geocoding skipped: API key is missing/blank");
            return null;
        }
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = "https://maps.googleapis.com/maps/api/geocode/json" +
                "?address=" + encodedAddress +
                "&key=" + apiKey;

        Map response = restTemplate.getForObject(url, Map.class);
        System.out.println("GEOCODE response = " + response);

        if (response == null || !response.get("status").equals("OK")) {
            return null;
        }

        var results = (java.util.List<Map>) response.get("results");
        var geometry = (Map) results.get(0).get("geometry");
        var location = (Map) geometry.get("location");

        Double lat = ((Number) location.get("lat")).doubleValue();
        Double lng = ((Number) location.get("lng")).doubleValue();

        return new Double[]{lat, lng};
    }
}
