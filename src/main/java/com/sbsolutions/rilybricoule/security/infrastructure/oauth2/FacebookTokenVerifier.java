package com.sbsolutions.rilybricoule.security.infrastructure.oauth2;

import com.sbsolutions.rilybricoule.dto.SocialUserInfo;
import com.sbsolutions.rilybricoule.entity.AuthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class FacebookTokenVerifier implements OAuth2TokenVerifier {

    private final WebClient webClient;
    private final String appId;
    private final String appSecret;

    public FacebookTokenVerifier(
            @Value("${oauth2.facebook.app-id}") String appId,
            @Value("${oauth2.facebook.app-secret}") String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.webClient = WebClient.builder()
                .baseUrl("https://graph.facebook.com")
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public SocialUserInfo verify(String idToken) {
        Map<String, Object> debugResponse = webClient.get()
                .uri("/debug_token?input_token={token}&access_token={appId}|{appSecret}",
                        idToken, appId, appSecret)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (debugResponse == null) {
            throw new IllegalArgumentException("Failed to verify Facebook token");
        }

        Map<String, Object> data = (Map<String, Object>) debugResponse.get("data");
        if (data == null || !Boolean.TRUE.equals(data.get("is_valid"))) {
            throw new IllegalArgumentException("Invalid Facebook access token");
        }

        String userId = (String) data.get("user_id");

        Map<String, Object> userInfo = webClient.get()
                .uri("/{userId}?fields=id,email,first_name,last_name,picture&access_token={token}",
                        userId, idToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (userInfo == null || userInfo.get("email") == null) {
            throw new IllegalArgumentException("Could not retrieve email from Facebook");
        }

        String photoUrl = null;
        Map<String, Object> picture = (Map<String, Object>) userInfo.get("picture");
        if (picture != null) {
            Map<String, Object> pictureData = (Map<String, Object>) picture.get("data");
            if (pictureData != null) {
                photoUrl = (String) pictureData.get("url");
            }
        }

        return SocialUserInfo.builder()
                .email((String) userInfo.get("email"))
                .firstName((String) userInfo.get("first_name"))
                .lastName((String) userInfo.get("last_name"))
                .providerId(userId)
                .photoUrl(photoUrl)
                .build();
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.FACEBOOK;
    }
}
