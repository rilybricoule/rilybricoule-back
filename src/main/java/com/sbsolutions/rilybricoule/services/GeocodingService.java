package com.sbsolutions.rilybricoule.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    @Value("${google.maps.api.key:}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    public Double[] getCoordinates(String address) {

        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        String url = "https://maps.googleapis.com/maps/api/geocode/json" +
                "?address=" + address.replace(" ", "+") +
                "&key=" + apiKey;

        Map response = restTemplate.getForObject(url, Map.class);

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
