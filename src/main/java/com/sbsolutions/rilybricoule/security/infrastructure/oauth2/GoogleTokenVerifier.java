package com.sbsolutions.rilybricoule.security.infrastructure.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sbsolutions.rilybricoule.dto.SocialUserInfo;
import com.sbsolutions.rilybricoule.entity.AuthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifier implements OAuth2TokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${oauth2.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    @Override
    public SocialUserInfo verify(String idToken) {
        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            return SocialUserInfo.builder()
                    .email(payload.getEmail())
                    .firstName((String) payload.get("given_name"))
                    .lastName((String) payload.get("family_name"))
                    .providerId(payload.getSubject())
                    .photoUrl((String) payload.get("picture"))
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify Google token: " + e.getMessage());
        }
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }
}
